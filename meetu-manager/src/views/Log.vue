<script setup>
import { nextTick, reactive, ref} from "vue";
import { delBatch, logPage,  delOne, saveOrUpdate } from "@/api/logApi";
import JsonViewer from 'vue-json-viewer'
import { useUserStore } from "@/stores/user";
import { ElMessage } from "element-plus";
import 'vue-json-viewer/style.css'

const name = ref('')
const pageNum = ref(1)
const pageSize = ref(5)
const total = ref(0)
const data = reactive({
  table: [],
  pageMenus: useUserStore().getPageMenus(),
})

// 对话框
const dialogData = reactive({
  dialogFormVisible: false,
  title: '',
  formData: {}
})

// 加载数据
const load = () => {
  logPage({
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
const searchlog = () => {
  load()
}

// 重置搜索框
const resetSearch = () => {
  name.value = ''
  pageNum.value = 1
  pageSize.value = 5
  load()
}

// 编辑用户信息 随便查看详情
const dialogEdit = (row) => {
  dialogData.title = '日志详情'
  resetDialog(row)
}
// 重置 对话框
const resetDialog = (data) => {
  dialogData.dialogFormVisible = true
  nextTick(() => {
    // dialogData.formData = JSON.parse(JSON.stringify(data))
    dialogData.formData = data
    dialogData.formData.params = JSON.parse(data.params)
    dialogData.formData.output = JSON.parse(data.output)
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

const importDataUrl = ref('/api/log/import')
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
    ElMessage.success("导入日志成功!")
    load()
  } else {
    ElMessage.error(response.msg)
  }
}

// 导出数据
const exportAll = () => {
 window.open("http://localhost:8848/api/log/export")
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
      saveOrUpdate('/log', dialogData.formData).then(res => {
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
      <el-input v-model="name" placeholder="请输入日志关键字"/>
      <el-button
          type="primary"
          @click="searchlog"
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
              v-show="data.pageMenus.includes('log.export')"
              type="primary"
              style="color: white"
              @click="exportAll"
          >
            <el-icon style="vertical-align: middle">
              <Top />
            </el-icon>
            <span style="vertical-align: middle">导出</span>
          </el-button>

<!--          <el-upload-->
<!--              v-show="data.pageMenus.includes('log.import')"-->
<!--              class="upload-box"-->
<!--              :show-file-list="false"-->
<!--              :action="importDataUrl"-->
<!--              :accept="acceptTypes"-->
<!--              :headers="importHeaders"-->
<!--              :before-upload="beforeImport"-->
<!--              :on-error="importError"-->
<!--              :on-success="importSuccess"-->
<!--          >-->
<!--            <el-button-->
<!--                type="primary"-->
<!--                style="color: white"-->
<!--            >-->
<!--              <el-icon style="vertical-align: middle">-->
<!--                <Bottom />-->
<!--              </el-icon>-->
<!--              <span style="vertical-align: middle">导入</span>-->
<!--            </el-button>-->
<!--          </el-upload>-->

          <!-- 批量删除 -->
          <el-popconfirm title="您确定要执行此操作吗？" @confirm="confirmDelBatch">
            <template #reference>
              <el-button
                  v-show="data.pageMenus.includes('log.deleteBatch')"
                  type="danger"
                  style="color: white; margin-left: 15px"
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

        <el-table-column prop="id" label="编号"></el-table-column>
        <el-table-column prop="name" label="操作"></el-table-column>
        <el-table-column prop="params" label="入参" show-overflow-tooltip></el-table-column>
        <el-table-column prop="output" label="出参" show-overflow-tooltip></el-table-column>
        <el-table-column prop="url" label="url" show-overflow-tooltip></el-table-column>
        <el-table-column prop="duration" label="执行时间"></el-table-column>
        <el-table-column prop="ip" label="IP"></el-table-column>
        <el-table-column prop="address" label="地址" show-overflow-tooltip></el-table-column>
        <el-table-column prop="username" label="操作人"></el-table-column>


       <el-table-column label="操作" width="180">
         <template #default="scope">
           <el-button
               v-show="data.pageMenus.includes('log.edit')"
               type="primary"
               @click="dialogEdit(scope.row)"
           >
             详情
           </el-button>

           <el-popconfirm title="您确定要删除吗？" @confirm="del(scope.row)">
             <template #reference>
               <el-button
                   type="danger"
                   v-show="data.pageMenus.includes('log.delete')"
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

    <el-drawer
        v-model="dialogData.dialogFormVisible"
        :title="dialogData.title"
        :size="'45%'"
    >
      <el-descriptions
          class="margin-top"
          :column="4"
          size="large"
          border
      >
        <el-descriptions-item :span="2">
          <template #label>
            <div class="cell-item" >
              操作人
            </div>
          </template>
          {{ dialogData.formData.username }}
        </el-descriptions-item>
        <el-descriptions-item :span="2">
          <template #label>
            <div class="cell-item">
              操作
            </div>
          </template>
          {{ dialogData.formData.name }}
        </el-descriptions-item>
        <el-descriptions-item :span="2">
          <template #label>
            <div class="cell-item">
              执行时间
            </div>
          </template>
          {{ dialogData.formData.duration}}
        </el-descriptions-item>
        <el-descriptions-item :span="2">
          <template #label>
            <div class="cell-item">
              用户IP
            </div>
          </template>
          {{ dialogData.formData.ip}}
        </el-descriptions-item>
        <el-descriptions-item :span="2">
          <template #label>
            <div class="cell-item">
              用户地址
            </div>
          </template>
          {{ dialogData.formData.address }}
        </el-descriptions-item>
        <el-descriptions-item :span="2">
          <template #label>
            <div class="cell-item">
              服务URL
            </div>
          </template>
          {{ dialogData.formData.url }}
        </el-descriptions-item>
        <el-descriptions-item :span="4">
          <template #label>
            <div class="cell-item">
              入参
            </div>
          </template>
          <json-viewer
              :value="dialogData.formData.params"
              expanded
              copyable
              boxed
              sort
          ></json-viewer>
        </el-descriptions-item>
        <el-descriptions-item :span="4">
          <template #label>
            <div class="cell-item">
              出参
            </div>
          </template>
          <json-viewer
              :value="dialogData.formData.output"
              expanded
              copyable
              boxed
              sort
          ></json-viewer>
        </el-descriptions-item>

      </el-descriptions>
    </el-drawer>

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
