<script setup>
import {nextTick, onBeforeUnmount, reactive, ref, shallowRef} from "vue";
import {delBatch, dynamicPage, delOne, saveOrUpdate} from "@/api/dynamicApi";
import {useUserStore} from "@/stores/user";
import {ElMessage} from "element-plus";
import '@wangeditor/editor/dist/css/style.css' // 引入 css
import {Editor, Toolbar} from '@wangeditor/editor-for-vue'
import {getUserAll} from "@/api/userApi";



const name = ref('')
const pageNum = ref(1)
const pageSize = ref(5)
const total = ref(0)
const data = reactive({
  table: [],
  pageMenus: useUserStore().getPageMenus(),
  userList: [],
  noticeList: [],
})
getUserAll().then(res => data.userList = res.data)
// noticeAll().then(res => data.noticeList = res.data)

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
    {required: true, message: '请输入动态名称', trigger: 'blur'},
  ],
})

// 加载数据
const load = () => {
  dynamicPage({
    name: name.value,
    pageNum: pageNum.value,
    pageSize: pageSize.value
  }).then(res => {
    if (res.code === '200') {
      data.table = res.data.records
      total.value = res.data.total
    }
  })
}
load()

// 搜索用户
const searchdynamic = () => {
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
  dialogData.title = '新增动态'
  resetDialog({})
}
// 编辑用户信息 随便查看详情
const dialogEdit = (row) => {
  dialogData.title = '编辑动态'
  resetDialog(row)
}
// 重置 对话框
const resetDialog = (data) => {
  dialogData.dialogFormVisible = true
  nextTick(() => {
    dialogData.formData = JSON.parse(JSON.stringify(data))
    valueHtml.value = dialogData.formData.content
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
  idArr.value = selectionData.map(item => item.id)
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

const importDataUrl = ref('/api/dynamic/import')
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
    ElMessage.success("导入动态成功!")
    load()
  } else {
    ElMessage.error(response.msg)
  }
}

// 导出数据
const exportAll = () => {
  window.open("http://localhost:8848/api/dynamic/export")
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
    dialogData.formData.content = valueHtml.value
    if (valid) {
      saveOrUpdate('/dynamic', dialogData.formData).then(res => {
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

const content = ref('')
const viewShow = ref(false)
const view = (value) => {
  viewShow.value = true
  content.value = value
}
const valueHtml = ref('')  // 富文本内容
const editorRef = shallowRef()
const editorConfig = {
  placeholder: '请输入内容...',
  MENU_CONF: {
    uploadImage: {
      disabled: true
    },

  }
}
const handleCreated = (editor) => {
  editorRef.value = editor // 记录 editor 实例，重要！
}
onBeforeUnmount(() => {
  const editor = editorRef.value
  if (editor == null) return
  editor.destroy()
})

const handleImportSuccess = (res) => {

  dialogData.formData.img = res.data
  ElMessage.success("上传成功")
}


</script>

<template>
  <div class="home">

    <div class="main-search">
      <el-input v-model="name" placeholder="请输入动态关键字"/>
      <el-button
          type="primary"
          @click="searchdynamic"
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
          v-show="data.pageMenus.includes('dynamic.add')"
          type="primary"
          style="color: white"
          color="#00bd16"
          @click="dialogAdd"
      >
        <el-icon style="vertical-align: middle">
          <Plus/>
        </el-icon>
        <span style="vertical-align: middle">新增</span>
      </el-button>

      <el-button
          v-show="data.pageMenus.includes('dynamic.export')"
          type="primary"
          style="color: white"
          @click="exportAll"
      >
        <el-icon style="vertical-align: middle">
          <Top/>
        </el-icon>
        <span style="vertical-align: middle">导出</span>
      </el-button>

      <el-upload
          v-show="data.pageMenus.includes('dynamic.import')"
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
            <Bottom/>
          </el-icon>
          <span style="vertical-align: middle">导入</span>
        </el-button>

      </el-upload>

      <!-- 批量删除 -->
      <el-popconfirm title="您确定要执行此操作吗？" @confirm="confirmDelBatch">
        <template #reference>
          <el-button
              v-show="data.pageMenus.includes('dynamic.deleteBatch')"
              type="danger"
              style="color: white"
          >
            <el-icon style="vertical-align: middle">
              <Delete/>
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
      >
        <el-table-column type="selection" width="55"/>
        <el-table-column prop="id" label="编号"></el-table-column>
        <el-table-column prop="name" label="名称"></el-table-column>
        <el-table-column label="预览">
          <template #default="scope">
            <el-button @click="view(scope.row.content)">查看</el-button>
          </template>
        </el-table-column>
        <el-table-column label="图片">
          <template #default="scope">
            <el-image preview-teleported style="width: 80px; height: 80px" :src="scope.row.img"
                      :preview-src-list="[scope.row.img]"></el-image>
          </template>
        </el-table-column>
        <el-table-column label="用户">
          <template #default="scope">
            <span v-if="scope.row.userId">
              {{  data.userList.find(v => v.id === scope.row.userId) ? data.userList.find(v => v.id === scope.row.userId).name : '' }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="time" label="时间"></el-table-column>

        <el-table-column label="操作" width="180">
          <template #default="scope">
            <el-button
                v-show="data.pageMenus.includes('dynamic.edit')"
                type="primary"
                @click="dialogEdit(scope.row)"
            >
              编辑
            </el-button>

            <el-popconfirm title="您确定要删除吗？" @confirm="del(scope.row)">
              <template #reference>
                <el-button
                    type="danger"
                    v-show="data.pageMenus.includes('dynamic.delete')"
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

        <el-form-item prop="name" label="名称">
          <el-input v-model="dialogData.formData.name" autocomplete="off"></el-input>
        </el-form-item>

        <el-form-item prop="descr" label="描述">
          <el-input v-model="dialogData.formData.descr" autocomplete="off"></el-input>
        </el-form-item>

        <el-form-item prop="img" label="图片">
          <el-upload
              class="avatar-uploader"
              :show-file-list="false"
              :action="'/api/file/upload'"
              :on-success="handleImportSuccess"
              :headers="{ Authorization: useUserStore().getAuthorization}"
          >
            <img v-if="dialogData.formData.avatar" :src="dialogData.formData.avatar" class="avatar" />
            <el-icon v-else class="avatar-uploader-icon"><Plus /></el-icon>
          </el-upload>
        </el-form-item>

        <Toolbar
            style="border-bottom: 1px solid #ccc"
            :editor="editorRef"
            :mode="'simple'"
        />
        <Editor
            style="height: 300px; overflow-y: hidden;"
            v-model="valueHtml"
            :defaultConfig="editorConfig"
            :mode="'simple'"
            @onCreated="handleCreated"
        />

      </el-form>

      <template #footer>
          <span class="dialog-footer">
            <el-button @click="closeDialog">关闭</el-button>
            <el-button
                type="primary" @click="save"
            >
              保存
            </el-button>
          </span>
      </template>
    </el-dialog>

    <el-dialog v-model="viewShow" title="预览" width="60%">
      <div id="editor-content-view" class="editor-content-view" v-html="content" style="padding: 0 20px"></div>
      <template #footer>
      <span class="dialog-footer">
        <el-button @click="viewShow = false">关闭</el-button>
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

  /deep/ .el-dialog {
    .el-dialog__body {
      .editor-content-view {
        img {
          width: 100%;
        }
    }

    }
  }
}
</style>
