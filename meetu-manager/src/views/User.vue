<script setup>
import {nextTick, reactive, ref} from "vue";
import {delBatch, delUser, saveOrUpdate, userPage} from "@/api/userApi";
import {useUserStore} from "@/stores/user";
import {ElMessage} from "element-plus";
import {roleAll} from "@/api/roleApi";

const name = ref('')
const pageNum = ref(1)
const pageSize = ref(5)
const total = ref(0)
const data = reactive({
  table: [],
  roles: [],
  pageMenus: useUserStore().getPageMenus(),
  menus: useUserStore().getMenus()
})
const loading = ref(true)
const loadFlag = ref(0)

// 对话框
let dialogFormRef = ref()
const dialogData = reactive({
  dialogFormVisible: false,
  title: '',
  formData: {}
})
const dialogRules = reactive({
  username: [
    { required: true, message: '账号不能为空', trigger: 'blur' },
  ],
  age: [
    { required: true, message: '年龄不能为空', trigger: 'blur' },
    { min: 0, max: 120, message: '年龄范围在0到120之间', trigger: 'blur'}
  ],
  email: [
    { required: true, message: '邮箱不能为空', trigger: 'blur' },
  ],
  role: [
    { required: true, message: '请选择角色', trigger: 'blur' },
  ]
})

// 加载数据
const load = () => {
  userPage({
    name: name.value,
    pageNum: pageNum.value,
    pageSize: pageSize.value
  }).then(res => {
    if (res.code === '200') {
      data.table = res.data.records
      total.value = res.data.total
      checkLoading()
    }
  })

  // 更新权限
  roleAll().then(res => {
    if (res.code === '200') {
      data.roles = res.data
      checkLoading()
    }
  })
}
load()
const checkLoading = () => {
  loadFlag.value = loadFlag.value + 1
  console.log(loadFlag.value)
  if (loadFlag.value >= 2) {
    loading.value = false
  }
}


// 搜索用户
const searchUser = () => {
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
  dialogData.title = '新增用户'
  resetDialog({})
}
// 编辑用户信息 随便查看详情
const dialogEdit = (row) => {
  dialogData.title = '编辑用户'
  resetDialog(row)
}
// 重置 对话框
const resetDialog = (data) => {
  dialogData.dialogFormVisible = true
  nextTick(() => {
    dialogData.formData = JSON.parse(JSON.stringify(data))
    dialogFormRef.value.resetFields()
  })
}
// 关闭对话框
const closeDialog = () => {
  dialogData.dialogFormVisible = false
}

// 删除
const del = (data) => {
  delUser(data.id).then(res => {
    if (res.code === '200') {
      ElMessage.success("删除成功")
      load()
    }
  })
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

const importDataUrl = ref('/api/user/import')
const importHeaders = reactive({
  Authorization: useUserStore().getAuthorization
})
const acceptTypes = ref('"application/vnd.ms-excel,application/vnd.openxmlformats-officedocument.spreadsheetml.sheet')
// 限制文件上传的裂隙
const beforeImport = (file) => {
  let isExcel = file.type === "application/vnd.ms-excel" || file.type === "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
  if (!isExcel) {
    ElMessage.warning("仅仅支持上传Excel类型的文件")
  }
  return isExcel;
}
// 上传失败
const importError = (response) => {
  console.log("导入失败", response)
}
// 上传成功
const importSuccess = (response) => {
  let code = response.code
  if (code === '200') {
    ElMessage.success("导入成功! 未设置密码的用户默认密码是 123abc")
    load()
  } else {
    ElMessage.error(response.msg)
  }
}

// 导出数据
const exportAll = () => {
  // TODO 这里肯定是有问题的
 window.open("http://localhost:8848/api/user/export")
}
const save = () => {
  dialogFormRef.value.validate(valid => {
   if (valid) {
     saveOrUpdate('/user/modify', dialogData.formData).then(res => {
       if (res.code === '200') {
         dialogData.dialogFormVisible = false
         ElMessage.success('操作成功')
         load()
       } else {
         ElMessage.error(res.msg)
       }
     })
   } else {
     ElMessage.warning('请将表单填写完整')
   }
  })
}

</script>

<template>
  <div class="home">

    <div class="main-search">
      <el-input v-model="name" placeholder="请输入用户账户"/>
      <el-button
          type="primary"
          @click="searchUser"
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
          v-show="data.pageMenus.includes('user.add')"
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

      <el-button
          v-show="data.pageMenus.includes('user.import')"
          type="primary"
          style="color: white"
          @click="exportAll"
      >
        <el-icon style="vertical-align: middle">
          <Top />
        </el-icon>
        <span style="vertical-align: middle">导出</span>
      </el-button>

      <el-upload
          v-show="data.pageMenus.includes('user.export')"
          class="upload-box"
          :show-file-list="false"
          :action="importDataUrl"
          :accept="acceptTypes"
          :headers="importHeaders"
          :before-upload="beforeImport"
          :on-error="importError"
          :on-success="importSuccess"
      >
        <el-button
            type="primary"
            style="color: white"
        >
          <el-icon style="vertical-align: middle">
            <Bottom />
          </el-icon>
          <span style="vertical-align: middle">导入</span>
        </el-button>

      </el-upload>

      <!-- 批量删除 -->
      <el-popconfirm title="您确定要执行此操作吗？" @confirm="confirmDelBatch">
        <template #reference>
          <el-button
              v-show="data.pageMenus.includes('user.deleteBatch')"
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
          style="width: 100%;"
          v-loading="loading"
      >
        <el-table-column type="selection" width="55"/>
        <el-table-column prop="username" label="账户" />
        <el-table-column prop="name" label="昵称" />
        <el-table-column prop="gender" label="性别" />
        <el-table-column prop="email" label="邮箱" />
        <el-table-column prop="role" label="角色" >
          <template #default="scope">
           <span v-if="data.roles.length"> {{ data.roles.find(item => item.flag === scope.row.role).name }} </span>
          </template>
        </el-table-column>

        <el-table-column prop="createTime" label="注册时间" />
        <el-table-column label="操作" width="180">
          <template #default="scope">
            <el-button
                v-show="data.pageMenus.includes('user.edit')"
                type="primary"
                @click="dialogEdit(scope.row)"
            >
              编辑
            </el-button>

            <el-popconfirm title="您确定要删除吗？" @confirm="del(scope.row)">
              <template #reference>
                <el-button
                    type="danger"
                    v-show="data.pageMenus.includes('user.delete')"
                >
                  删除
                </el-button>
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
          @keyup.enter="save"
      >
        <el-form-item prop="username" label="账号">
          <el-input
              v-model="dialogData.formData.username"
              placeholder="请填入账号"
              clearable

              :disabled="dialogData.title !== undefined && dialogData.title === '编辑用户'"
          />
        </el-form-item>
        <el-form-item prop="email" label="邮箱">
          <el-input
              v-model="dialogData.formData.email"
              placeholder="请填入邮箱"
          />
        </el-form-item>
        <el-form-item prop="name" label="昵称">
          <el-input
              v-model="dialogData.formData.name"
              placeholder="请填入昵称"

          />
        </el-form-item>
        <el-form-item prop="role" label="角色">
          <el-select
            v-model="dialogData.formData.role"
            style="width: 100%"
          >
            <el-option
                v-for="item in data.roles"
                :label="item.name"
                :value="item.flag"
                :key="item.id"
            ></el-option>
          </el-select>
        </el-form-item>
        <el-form-item prop="type" label="性别">
          <el-radio-group v-model="dialogData.formData.gender">
            <el-radio label="未知">未知</el-radio>
            <el-radio label="男">男</el-radio>
            <el-radio label="女">女</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item prop="age" label="年龄">
          <el-slider
              v-model="dialogData.formData.age"
              show-input
          />
        </el-form-item>
        <el-form-item prop="intro" label="简介">
          <el-input v-model="dialogData.formData.intro" type="textarea" />
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
}
</style>
