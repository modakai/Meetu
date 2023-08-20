<script setup>

import { nextTick, reactive, ref} from "vue";
import { delBatch, rolePage, delOne, saveOrUpdate} from "@/api/roleApi";
import {useUserStore} from "@/stores/user";
import {ElMessage} from "element-plus";
import {permissionTree} from "@/api/permissionApi";
import {logoutAdmin} from "@/api/userApi";
import router from "@/router";

const name = ref('')
const pageNum = ref(1)
const pageSize = ref(5)
const total = ref(0)
const data = reactive({
  table: [],
  permissionTable: []
})

const permissionTreeRef = ref()

// 对话框
let dialogFormRef = ref()
const dialogData = reactive({
  dialogFormVisible: false,
  title: '',
  formData: {}
})
// TODO 修改自己修改的地方
const dialogRules = reactive({
  name: [
    { required: true, message: '权限名称不能为空', trigger: 'blur' },
  ],
  flag: [
    { required: true, message: '权限唯一不能为空', trigger: 'blur' },
  ],
})

// 加载数据
const load = () => {
  rolePage({
    name: name.value,
    pageNum: pageNum.value,
    pageSize: pageSize.value
  }).then(res => {
    if (res.code === '200') {
      data.table = res.data.records
      total.value = res.data.total
    }
  })

  // 获取权限
  permissionTree().then(res => {
    if (res.code === '200') {
      data.permissionTable = res.data
    } else {
      ElMessage.error(res.msg)
    }
  })
}
load()

// 搜索用户
const searchrole = () => {
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
  dialogData.title = '新增角色'
  resetDialog({})
}
// 编辑用户信息 随便查看详情
const dialogEdit = (row) => {
  dialogData.title = '编辑角色'
  resetDialog(row)
}
// 重置 对话框
const resetDialog = (data) => {
  dialogData.dialogFormVisible = true
  nextTick(() => {
    dialogData.formData = JSON.parse(JSON.stringify(data))
    dialogFormRef.value.resetFields()
    if (!dialogData.formData.permissionIds.length) {  // 设置无任何节点选择的状态
      permissionTreeRef.value.setCheckedKeys([])
    }
    dialogData.formData.permissionIds.forEach(v => {
      permissionTreeRef.value.setChecked(v, true, false)  // 给权限树设置选中的节点
    })
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

const admin = ref(useUserStore().getManagerInfo)
const save = () => {
  dialogFormRef.value.validate(valid => {   // valid就是校验的结果
    if (valid) {
      // 全选菜单
      let checkedKeys = permissionTreeRef.value.getCheckedKeys()

      let halfCheckedKeys = permissionTreeRef.value.getHalfCheckedKeys();

      checkedKeys.unshift.apply(checkedKeys, halfCheckedKeys)

      dialogData.formData.flag = dialogData.formData.flag.toUpperCase()
      dialogData.formData.permissionIds = checkedKeys;
      let logoutFlag = false;
      if (dialogData.formData.flag === admin.value.role) {
        logoutFlag = true
      }
      saveOrUpdate('/role', dialogData.formData).then(res => {
        if (res.code === '200') {
          ElMessage.success('保存成功')
          dialogData.dialogFormVisible = false
          // 如果修改的是自己角色本身，就重新登入
          if (logoutFlag) {
            logout()
            return;
          }

          load()  // 刷新表格数据
        } else {
          ElMessage.error(res.msg)
        }
      })
    } else {
      ElMessage.warning('请将表单补充完整')
    }
  })
}

const logout = () => {
  // 发送请求
  logoutAdmin({
    uid: admin.value.uid,
    loginType: 'PC'
  }).then(res => {
    if (res.code === '200') {
      // 重载 manager中的数据
      useUserStore().reset()
      // 跳转登入页
      router.push("/login")

    } else {
      ElMessage.error(res.msg)
    }
  })

}
</script>

<template>
  <div class="home">

    <div class="main-search">
      <el-input v-model="name" placeholder="请输入角色关键字"/>
      <el-button
          type="primary"
          @click="searchrole"
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
          stripe border
          style="width: 100%;"
      >
       <el-table-column type="selection" width="55"/>
        <el-table-column prop="name" label="名称"></el-table-column>
        <el-table-column prop="flag" label="唯一标识"></el-table-column>

       <el-table-column label="操作" width="180">
         <template #default="scope">
           <el-button type="primary" @click="dialogEdit(scope.row)">编辑</el-button>
           <el-popconfirm title="您确定要删除吗？" @confirm="del(scope.row)">
             <template #reference>
               <el-button type="danger">删除</el-button>
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
        :title="dialogData.title"
        draggable :close-on-click-modal="false"
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

        <el-form-item prop="name" label="名称">
          <el-input v-model="dialogData.formData.name" autocomplete="off"></el-input>
        </el-form-item>
        <el-form-item prop="flag" label="唯一标识">
          <el-input v-model="dialogData.formData.flag" autocomplete="off"></el-input>
        </el-form-item>
        <el-form-item label="权限">
         <div class="permission-box">
           <el-tree
               ref="permissionTreeRef"
               :data="data.permissionTable"
               show-checkbox
               :props="{ label: 'name', value: 'id', children: 'children'}"
               :render-after-expand="false"
               default-expand-all
               node-key="id"
           />
         </div>
        </el-form-item>
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

  .permission-box {
    width: 100%;
    border: 1px solid #ccc;
    border-radius: 5px;
    padding: 5px;
  }
}
</style>
