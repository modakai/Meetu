<script setup>

import { nextTick, reactive, ref} from "vue";
import {delBatch, permissionPage, delOne, saveOrUpdate, permissionTree} from "@/api/permissionApi";
import {ElMessage} from "element-plus";
import {dictTypeList} from "@/api/dictApi";

const data = reactive({
  table: []
})
const loading = ref(true)
// 图标字典集合
const icons = ref([])

// 对话框
let dialogFormRef = ref()
const dialogData = reactive({
  dialogFormVisible: false,
  title: '',
  formData: {}
})
const dialogRules = reactive({
  name: [
    { required: true, message: '权限名称不能为空', trigger: 'blur' },
  ],
  type: [
    { required: true, message: '请选择对应的权限类型', trigger: 'blur' },
  ]
})

// 加载数据
const load = () => {
  permissionTree().then(res => {
    if (res.code === '200') {
      data.table = res.data
      loading.value = false
    }
  })

}
load()

// 新增对话框
const dialogAdd = () => {
  dialogData.title = '新增权限'
  resetDialog({type: 1, orders: 1})
}
// 编辑信息 查看详情
const dialogEdit = (row) => {
  dialogData.title = '编辑权限'
  let curData = row
  if (curData.pid === 0) {
    curData.pid = null
  }
  resetDialog(curData)
}
// 重置 对话框
const resetDialog = (data) => {
  dialogData.dialogFormVisible = true
  nextTick(() => {
    dialogData.formData = JSON.parse(JSON.stringify(data))
    dialogFormRef.value.resetFields()
  })
  dictTypeList('icon').then(res => {
    icons.value = res.data
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
      saveOrUpdate('/permission', dialogData.formData).then(res => {
        if (res.code === '200') {
          ElMessage.success('保存成功! 重新登入后生效')
          dialogData.dialogFormVisible = false
          load()  // 刷新表格数据
        } else {
          ElMessage.error(res.msg)
        }
      })
    }
  })
}



// 自定义业务 数据

// 父级选择器
const handleNodeClick = (data) => {
  if (data.id === dialogData.formData.id) {  // 当前编辑行的id跟选择的父节点的id如果相同的话，就不让他选择
    ElMessage.warning('父节点不能选择自身')

    nextTick(() => {  // 等树节点的dom渲染完之后再去修改pid
      // 重置pid
      dialogData.formData.pid = null
    })
  }
}

// 是否隐藏
const changeHide = (row) => {
  console.log(row)
  saveOrUpdate('/permission', row).then(res => {
    if (res.code === '200') {
      ElMessage.success('操作成功')
      load()  // 刷新表格数据
    } else {
      ElMessage.error(res.msg)
    }
  })
}
</script>

<template>
  <div class="home">


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
          row-key="id"
          :tree-props="{ children: 'children' }"
          v-loading="loading"
          style="width: 100%;"
      >
        <el-table-column type="selection" width="55"/>
        <el-table-column prop="id" label="编号"  />
        <el-table-column prop="name" label="名称"></el-table-column>
        <el-table-column prop="path" label="访问路径"></el-table-column>
        <el-table-column prop="page" label="页面路径"></el-table-column>
        <el-table-column prop="orders" label="顺序"></el-table-column>

        <el-table-column prop="icon" label="图标">
          <template #default="scope">
            <el-icon v-if="scope.row.icon" size="20px">
              <component :is="scope.row.icon"></component>
            </el-icon>
          </template>
        </el-table-column>

        <el-table-column prop="auth" label="权限" width="130"></el-table-column>
        <el-table-column prop="pid" label="父级"></el-table-column>
        <el-table-column prop="type" label="类型">
          <template #default="scope">
            <el-tag type="warning" v-show="scope.row.type === 1">菜单目录</el-tag>
            <el-tag  v-show="scope.row.type === 2">菜单页面</el-tag>
            <el-tag type="success" v-show="scope.row.type === 3">菜单按钮</el-tag>
          </template>
        </el-table-column>

        <el-table-column prop="hide" label="是否隐藏">
          <template #default="scope">
            <el-switch
                v-model="scope.row.hide"
                :active-value="1"
                :inactive-value="0"
                @click="changeHide(scope.row)"
            ></el-switch>
          </template>
        </el-table-column>


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
          @keyup.enter="save"
      >

        <el-form-item prop="type" label="类型">
          <el-radio-group v-model="dialogData.formData.type">
            <el-radio :label="1">菜单目录</el-radio>
            <el-radio :label="2">菜单页面</el-radio>
            <el-radio :label="3">页面按钮</el-radio>
          </el-radio-group>
        </el-form-item>

        <el-form-item prop="pid" label="父级" v-show="dialogData.formData.type !== 1">
          <el-tree-select
              style="width: 100%" v-model="dialogData.formData.pid" :data="data.table"
              @node-click="handleNodeClick"
              :props="{ label: 'name', value: 'id' }"
              :render-after-expand="false" check-strictly
          />
        </el-form-item>

        <el-form-item prop="name" label="权限名称">
          <el-input v-model="dialogData.formData.name" placeholder="请输入权限名称" autocomplete="off"></el-input>
        </el-form-item>
        <el-form-item prop="path" label="访问路径" v-show="dialogData.formData.type === 2">
          <el-input v-model="dialogData.formData.path" placeholder="请输入访问路径" autocomplete="off"></el-input>
        </el-form-item>
        <el-form-item prop="page" label="页面路径" v-show="dialogData.formData.type === 2">
          <el-input v-model="dialogData.formData.page" placeholder="请输入页面路径" autocomplete="off"></el-input>
        </el-form-item>
        <el-form-item prop="orders" label="顺序" v-show="dialogData.formData.type !== 3">
          <el-input-number v-model="dialogData.formData.orders" :min="1" />
        </el-form-item>

        <el-form-item prop="icon" label="图标" v-show="dialogData.formData.type !== 3">
          <el-select v-model="dialogData.formData.icon" placeholder="请选择图标" style="width: 100%;">
            <el-option
                v-for="item in icons"
                :key="item.id"
                :label="item.code"
                :value="item.content"
            >
              <el-icon>
                <component :is="item.content"></component>
              </el-icon>
              <span style="font-size: 14px;margin-left: 5px; top: -3px">{{ item.code }}</span>
            </el-option>
          </el-select>
        </el-form-item>

        <el-form-item prop="auth" label="权限" v-show="dialogData.formData.type !== 1">
          <el-input v-model="dialogData.formData.auth" placeholder="请输入权限; 按 user.add 填写" autocomplete="off"></el-input>
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

  .upload-box {
    display: inline-block;
    position: relative;
    top: 3px;
    margin: 0 12px;
  }
}
</style>
