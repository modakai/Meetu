<script setup>
import { nextTick, reactive, ref} from "vue";
import { delBatch, imPage,  delOne, saveOrUpdate } from "@/api/imApi";
import {useUserStore} from "@/stores/user";
import {ElMessage} from "element-plus";

const name = ref('')
const pageNum = ref(1)
const pageSize = ref(5)
const total = ref(0)
const data = reactive({
  table: [],
  pageMenus: useUserStore().getPageMenus(),
})
const loading = ref(true)

// 对话框
let dialogFormRef = ref()
const dialogData = reactive({
  dialogFormVisible: false,
  title: '',
  formData: {}
})
// TODO 修改自己修改的地方
const dialogRules = reactive({
  username: [
    { required: true, message: '账号不能为空', trigger: 'blur' },
  ],
})

// 加载数据
const load = () => {
  imPage({
    name: name.value,
    pageNum: pageNum.value,
    pageSize: pageSize.value
  }).then(res => {
    if (res.code === '200') {
      data.table = res.data.records
      total.value = res.data.total
      loading.value = false
    }
  })
}
load()

// 搜索用户
const searchim = () => {
  load()
}

// 重置搜索框
const resetSearch = () => {
  name.value = ''
  pageNum.value = 1
  pageSize.value = 5
  load()
}

// 新增对话框
const dialogAdd = () => {
  dialogData.title = '新增聊天室'
  resetDialog({})
}
// 编辑用户信息 随便查看详情
const dialogEdit = (row) => {
  dialogData.title = '编辑聊天室'
  resetDialog(row)
}
// 重置 对话框
const resetDialog = (data) => {
  dialogData.dialogFormVisible = true
  // console.log(dialogData.dialogFormVisible = true)
  nextTick(() => {
    dialogData.formData = JSON.parse(JSON.stringify(data))
    console.log(dialogData.formData)
    dialogFormRef.value.resetFields()
  })
}
// 关闭对话框
const closeDialog = () => {
  dialogData.dialogFormVisible = false
}

// 批量删除
const idArr = ref([])
const handleSelectionChange = (selectionData) => {
  idArr.value = selectionData.map(user => user.id)
}
// 批量删除
const confirmDelBatch = () => {
  if (!idArr.value || !idArr.value.length) {
    ElMessage.warning("请选择数据")
    return
  }

  delBatch(idArr.value).then(res => {
    if (res.code === '200') {
      ElMessage.success("批量删除成功")
      load()
    } else {
      ElMessage.error(res.msg)
    }
  })
}


// 删除
const del = (data) => {
  // 写接口
  delOne(data.id).then(res => {
    if (res.code === '200') {
      ElMessage.success('删除成功')
      load()
    } else {
      ElMessage.error(res.msg)
    }
  })
}

const save = () => {
  dialogFormRef.value.validate(valid => {   // valid就是校验的结果
    if (valid) {
      saveOrUpdate('/im', dialogData.formData).then(res => {
        if (res.code === '200') {
          ElMessage.success('保存成功')
          dialogData.dialogFormVisible = false
          load()  // 刷新表格数据
        } else {
          ElMessage.error(res.msg)
        }
      })
    }
  })
}


</script>

<template>
  <div class="home">

    <div class="main-search">
      <el-input v-model="name" placeholder="请输入聊天室关键字"/>
      <el-button
          type="primary"
          @click="searchim"
      >
        <el-icon style="vertical-align: middle">
          <Search/>
        </el-icon>
        <span style="vertical-align: middle">搜索</span>
      </el-button>
      <el-button
          type="primary"
          color="#eca336"
          @click="resetSearch"
          style="color: white"
      >
        <el-icon style="vertical-align: middle">
          <RefreshRight/>
        </el-icon>
        <span style="vertical-align: middle">重置</span>
      </el-button>

    </div>

    <div style="margin: 10px 0;">
      <el-button
          v-show="data.pageMenus.includes('im.add')"
          type="primary"
          style="color: white"
          color="#00bd16"
          @click="dialogAdd"
      >
        <el-icon style="vertical-align: middle">
          <Plus />
        </el-icon>
        <span style="vertical-align: middle">新增</span>
      </el-button>

      <!-- 批量删除 -->
      <el-popconfirm title="您确定要执行此操作吗？" @confirm="confirmDelBatch">
        <template #reference>
          <el-button
              v-show="data.pageMenus.includes('im.deleteBatch')"
              type="danger"
              style="color: white"
          >
            <el-icon style="vertical-align: middle">
              <Delete />
            </el-icon>
            <span style="vertical-align: middle">批量删除</span>
          </el-button>
        </template>
      </el-popconfirm>
    </div>

    <!-- 表格 -->
    <div style="margin: 20px 0">
      <el-table
          :data="data.table"
          @selection-change="handleSelectionChange"
          row-key="id"
          stripe border
          v-loading="loading"
          style="width: 100%;"
      >
       <el-table-column type="selection" width="55"/>

        <el-table-column prop="id" label="编号"></el-table-column>
        <el-table-column prop="uid" label="用户的id"></el-table-column>
        <el-table-column prop="username" label="用户账户"></el-table-column>
        <el-table-column prop="avatar" label="用户头像">
          <template #default="scope">
            <el-image :src="scope.row.avatar" v-show="scope.row.avatar" style="width: 80px"></el-image>
          </template>
        </el-table-column>
        <el-table-column prop="text" label="聊天消息"></el-table-column>
        <el-table-column label="图片">
          <template #default="scope">
            <el-image
                v-show="scope.row.img"
                preview-teleported
                style="width: 100px; height: 100px"
                :src="'http:localhost:8848'+ scope.row.img"
                :preview-src-list="['http:localhost:8848'+ scope.row.img]"
            >
            </el-image>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="发送时间"></el-table-column>

       <el-table-column label="操作" width="180">
         <template #default="scope">
           <el-button  v-show="data.pageMenus.includes('im.edit')" type="primary" @click="dialogEdit(scope.row)">编辑</el-button>
           <el-popconfirm title="您确定要删除吗？" @confirm="del(scope.row)">
             <template #reference>
               <el-button  v-show="data.pageMenus.includes('im.delete')" type="danger">删除</el-button>
             </template>
           </el-popconfirm>
         </template>
       </el-table-column>
      </el-table>
    </div>

    <!--  分页导航   -->
    <div style="margin: 10px 0">
      <el-pagination
          @current-change="load"
          @size-change="load"
          v-model:current-page="pageNum"
          v-model:page-size="pageSize"
          background
          :page-sizes="[1, 5, 10, 20]"
          layout="total, sizes, prev, pager, next, jumper"
          :total="total"
      />
    </div>

    <!-- 对话框 -->
    <el-dialog
        v-model="dialogData.dialogFormVisible"
        :title="dialogData.title" draggable
        :close-on-click-modal="false"
        width="50%"
    >
      <el-form
          ref="dialogFormRef"
          :model="dialogData.formData"
          :rules="dialogRules"
          size="large"
          status-icon
          label-width="80px"
          style="padding: 0 20px"
      >

        <el-form-item prop="uid" label="用户的id">
          <el-input v-model="dialogData.formData.uid" autocomplete="off"></el-input>
        </el-form-item>
        <el-form-item prop="avatar" label="用户头像">
          <template #default="scope">
            <el-image :src="dialogData.formData.avatar" v-show="dialogData.formData.avatar" style="width: 80px" />
          </template>
        </el-form-item>
        <el-form-item prop="text" label="聊天消息">
          <el-input v-model="dialogData.formData.text" autocomplete="off"></el-input>
        </el-form-item>
<!--        <el-form-item prop="img" label="聊天图片">-->
<!--          <el-input v-model="dialogData.formData.img" autocomplete="off"></el-input>-->
<!--        </el-form-item>-->

      </el-form>

      <template #footer>
          <span class="dialog-footer">
            <el-button @click="closeDialog">关闭</el-button>
            <el-button type="primary" @click="save">
              保存
            </el-button>
          </span>
      </template>
    </el-dialog>

  </div>
</template>

<style scoped lang="less">
.home {
  // 搜索框
  .main-search {
    /deep/ .el-input {
      width: 300px;
    }

    /deep/ .el-button {
      margin-left: 10px;
    }
  }

  .upload-box {
    display: inline-block;
    position: relative;
    top: 3px;
    margin: 0 12px;
  }
}
</style>
