<script setup>
import {nextTick, onBeforeUnmount, reactive, ref, shallowRef} from "vue";
import {delBatch, commentsPage, delOne, saveOrUpdate, commentsTree} from "@/api/commentsApi";
import { useUserStore } from "@/stores/user";
import { ElMessage } from "element-plus";
import '@wangeditor/editor/dist/css/style.css' // 引入 css
import { Editor, Toolbar } from '@wangeditor/editor-for-vue'
import {dynamicAll} from "@/api/DynamicApi";
import {getUserAll} from "@/api/userApi";

const name = ref('')
const pageNum = ref(1)
const pageSize = ref(5)
const total = ref(0)
const data = reactive({
  table: [],
  pageMenus: useUserStore().getPageMenus(),
  dynamicOptions: [],
  userOptions: [],

})
dynamicAll().then(res => {
  if (res.code === '200') {
    data.dynamicOptions = res.data
  }
})
getUserAll().then(res => {
  data.userOptions = res.data
})

// 对话框
let dialogFormRef = ref()
const dialogData = reactive({
  dialogFormVisible: false,
  title: '',
  formData: {},
  commentsOptions: [],
  selectCommentsId: undefined
})
const dialogRules = reactive({
  dynamicId: [
    { required: true, message: '动态不能为空', trigger: 'blur' },
  ],
  time: [
    { required: true, message: '请选择时间', trigger: 'blur' },
  ],
  content: [
    { required: true, message: '请输入内容', trigger: 'blur' },
    { pattern: /^\s*(?=\r?$)\n/, message: "输入不能为空", trigger: 'blur'}
  ]
})

// 加载数据
const load = () => {
  commentsPage({
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
const searchcomments = () => {
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
  dialogData.title = '新增评论'
  dialogData.selectCommentsId = undefined
  dialogData.commentsOptions = []
  resetDialog({})
}
// 编辑用户信息 随便查看详情
const dialogEdit = (row) => {
  dialogData.title = '编辑评论'
  dialogData.selectCommentsId = row.id

  resetDialog(row)
}
// 重置 对话框
const resetDialog = (data) => {
  dialogData.dialogFormVisible = true
  nextTick(() => {
    dialogData.formData = JSON.parse(JSON.stringify(data))
    valueHtml.value = dialogData.formData.content
    dialogDynamicSelectChange()
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

const importDataUrl = ref('/api/comments/import')
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
    ElMessage.success("导入评论成功!")
    load()
  } else {
    ElMessage.error(response.msg)
  }
}

// 导出数据
const exportAll = () => {
 window.open("http://localhost:8848/api/comments/export")
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
  dialogData.formData.content = valueHtml.value
  dialogFormRef.value.validate(valid => {   // valid就是校验的结果
    if (valid) {
      saveOrUpdate('/comments', dialogData.formData).then(res => {
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
const valueHtml = ref('')  // 富文本内容
const editorConfig = {
  placeholder: '请输入内容...',
  MENU_CONF: {
    uploadImage: {
      disabled: true
    },

  }
}
const editorRef = shallowRef()
const handleCreated = (editor) => {
  editorRef.value = editor // 记录 editor 实例，重要！
}
onBeforeUnmount(() => {
  const editor = editorRef.value
  if (editor == null) return
  editor.destroy()
})

const dialogDynamicSelectChange = () => {
  let dynamicId = dialogData.formData.dynamicId;
  if (dynamicId) {
    commentsTree(Number.parseInt(dynamicId)).then(res => {
      dialogData.commentsOptions = res.data
    })
    console.log('搜索评论数据')
  } else {
    dialogData.commentsOptions = []
    dialogData.selectCommentsId = undefined
  }
}

const selectComments = () => {
  getSelectCommentsPid(dialogData.selectCommentsId)
}
const clearCommentsSelect = () => {
  dialogData.formData.pid = null
}
const getSelectCommentsPid = (commentId) => {
  let comments = null;
  dialogData.commentsOptions.forEach(item => {
    if (item.id === commentId) {
      comments = item
      return;
    }

    item.children.forEach(item => {
      if (item.id === commentId) {
        comments = item
        return;
      }
    })
  })


  if (comments.pid === null) {
    // 说明评论的是父级
    dialogData.formData.pid = null
    dialogData.formData.puserId = null
  } else {
    // 如果 pid 存在 那么就是回复二级评论
    dialogData.formData.pid = comments.pid
    dialogData.formData.puserId = comments.userId
  }
  console.log(dialogData.formData)
}

</script>

<template>
  <div class="home">

    <div class="main-search">
      <el-input v-model="name" placeholder="请输入评论关键字"/>
      <el-button
          type="primary"
          @click="searchcomments"
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
              v-show="data.pageMenus.includes('comments.add')"
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
              v-show="data.pageMenus.includes('comments.export')"
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
              v-show="data.pageMenus.includes('comments.import')"
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
                  v-show="data.pageMenus.includes('comments.deleteBatch')"
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
      >
       <el-table-column type="selection" width="55"/>

        <el-table-column prop="id" label="id"></el-table-column>
        <el-table-column prop="name" label="用户"></el-table-column>
        <el-table-column prop="dynamicName" label="动态"></el-table-column>
        <el-table-column prop="time" label="时间"></el-table-column>
        <el-table-column prop="location" label="属地"></el-table-column>

       <el-table-column label="操作" width="180">
         <template #default="scope">
           <el-button
               v-show="data.pageMenus.includes('comments.edit')"
               type="primary"
               @click="dialogEdit(scope.row)"
           >
             编辑
           </el-button>

           <el-popconfirm title="您确定要删除吗？" @confirm="del(scope.row)">
             <template #reference>
               <el-button
                   type="danger"
                   v-show="data.pageMenus.includes('comments.delete')"
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
        <el-form-item prop="dynamicId" label="动态">
          <el-select clearable v-model="dialogData.formData.dynamicId" @change="dialogDynamicSelectChange" placeholder="请选择"  style="width: 100%">
            <el-option v-for="item in data.dynamicOptions" :key="item.id" :label="item.name" :value="item.id"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="评论用户">
          <el-tree-select
              v-model="dialogData.selectCommentsId"
              :data="dialogData.commentsOptions"
              value-key="id"
              check-strictly
              clearable
              style="width: 100%"
              :render-after-expand="false"
              no-data-text="请先选择动态"
              placeholder="请选择评论用户"
              @change="selectComments"
              @clear="clearCommentsSelect"
          >
            <template #default="{ data: { name, content } }">
              {{ name }}<span style="color: gray">({{content}})</span></template
            >
          </el-tree-select>
        </el-form-item>
        <el-form-item prop="time" label="时间">
          <el-date-picker style="width: 100%" v-model="dialogData.formData.time" type="datetime" value-format="YYYY-MM-DD HH:mm:ss" placeholder="选择日期时间"></el-date-picker>
        </el-form-item>

        <div style="border: 1px solid #ccc">
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
        </div>
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
