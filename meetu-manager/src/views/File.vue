<script setup>
import { nextTick, reactive, ref} from "vue";
import {delBatch, filePage, delOne, downFileBefore} from "@/api/fileApi";
import { useUserStore } from "@/stores/user";
import { ElMessage } from "element-plus";

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
const dialogData = reactive({
  dialogFormVisible: false,
  title: '',
  formData: {}
})

// 加载数据
const load = () => {
  filePage({
    name: name.value,
    pageNum: pageNum.value,
    pageSize: pageSize.value
  }).then(res => {
    console.log(res)
    if (res.code === '200') {
      data.table = res.data.records
      total.value = res.data.total
      loading.value = false
    }
  })
}
load()

// 搜索用户
const searchfile = () => {
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
  dialogData.title = '新增文件'
  resetDialog({})
}
// 编辑用户信息 随便查看详情
const dialogEdit = (row) => {
  dialogData.title = '文件详情'
  resetDialog(row)
}
// 重置 对话框
const resetDialog = (data) => {
  dialogData.dialogFormVisible = true
  nextTick(() => {
    dialogData.formData = JSON.parse(JSON.stringify(data))
  })
}
// 关闭对话框
const closeDialog = () => {
  dialogData.dialogFormVisible = false
}

// 批量删除
const idArr = ref([])
const fileUrlArr = ref([])
const handleSelectionChange = (selectionData) => {
  idArr.value = selectionData.map(file => file.id)
  console.log(idArr.value)
  fileUrlArr.value = selectionData.map(item => item.location)
}
// 批量删除
const confirmDelBatch = () => {
  if (!idArr.value || !idArr.value.length) {
    ElMessage.warning("请选择数据")
    return
  }

  delBatch({}).then(res => {
    if (res.code === '200') {
      ElMessage.success("批量删除成功")
      load()
    } else {
      ElMessage.error(res.msg)
    }
  })
}


// 导出数据
const exportAll = () => {
  // TODO 这里肯定是有问题的
 window.open("http://localhost:8848/api/file/export")
}

// 删除
const del = (data) => {
  console.log({simpleFileId: data.id, simpleFileUrl: data.location})
  // 写接口
  delOne({simpleFileId: data.id, simpleFileUrl: data.location}).then(res => {
    if (res.code === '200') {
      ElMessage.success('删除成功')
      load()
    } else {
      ElMessage.error(res.msg)
    }
  })
}
// 下载文件
const downFile = (row) => {
  console.log(row.location)
  downFileBefore(row.location).then(res => {
    if (res.code === '200') {
      // 发送下载请求
      window.open("http://localhost:8848/api/file/download/file?fileName=" + res.data)
    } else if (res.code === '404') {
      ElMessage.warning(res.msg)
    } else {
      ElMessage.error(res.msg)
    }
  })
  // window.open("http://localhost:8848/api/file/download?fileName=" + row.location)
}


</script>

<template>
  <div class="home">

    <div class="main-search">
      <el-input v-model="name" placeholder="请输入文件关键字"/>
      <el-button
          type="primary"
          @click="searchfile"
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
              v-show="data.pageMenus.includes('file.export')"
              type="primary"
              style="color: white"
              @click="exportAll"
          >
            <el-icon style="vertical-align: middle">
              <Top />
            </el-icon>
            <span style="vertical-align: middle">导出</span>
          </el-button>


          <!-- 批量删除 -->
          <el-popconfirm title="您确定要执行此操作吗？" @confirm="confirmDelBatch">
            <template #reference>
              <el-button
                  v-show="data.pageMenus.includes('file.deleteBatch')"
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
          v-loading="loading"
          style="width: 100%;"
      >
       <el-table-column type="selection" width="55"/>

        <el-table-column prop="id" label="编号"></el-table-column>
        <el-table-column prop="name" label="文件名"></el-table-column>
        <el-table-column prop="size" label="大小"></el-table-column>
        <el-table-column prop="type" label="类型"></el-table-column>
        <el-table-column
            prop="url"
            label="访问链接"
            show-overflow-tooltip
        ></el-table-column>
<!--        <el-table-column prop="md5" label="文件摘要"></el-table-column>-->
        <el-table-column
            prop="location"
            label="存储地址"
            show-overflow-tooltip
        ></el-table-column>



       <el-table-column label="操作" width="240">
         <template #default="scope">
           <el-button
               type="primary"
               @click="dialogEdit(scope.row)"
           >
             详情
           </el-button>

           <el-button
               color="#5ecf6b"
               style="color: #ffffff"
               @click="downFile(scope.row)"
           >
             下载
           </el-button>

           <el-popconfirm title="您确定要删除吗？" @confirm="del(scope.row)">
             <template #reference>
               <el-button
                   type="danger"
                   v-show="data.pageMenus.includes('file.delete')"
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
      <el-descriptions
          class="margin-top"
          title=""
          :column="3"
          size="large"
          border
      >
        <el-descriptions-item>
          <template #label>
            <div class="cell-item">
              上传时文件名
            </div>
          </template>
          {{ dialogData.formData.name }}
        </el-descriptions-item>
        <el-descriptions-item>
          <template #label>
            <div class="cell-item">
              文件大小
            </div>
          </template>
          {{ dialogData.formData.size }}
        </el-descriptions-item>
        <el-descriptions-item>
          <template #label>
            <div class="cell-item">
              文件类型
            </div>
          </template>
          {{ dialogData.formData.type }}
        </el-descriptions-item>
        <el-descriptions-item :span="3">
          <template #label>
            <div class="cell-item">
              存储地址
            </div>
          </template>
          {{ dialogData.formData.location }}
        </el-descriptions-item>
        <el-descriptions-item :span="3">
          <template #label>
            <div class="cell-item">
              访问链接
            </div>
          </template>
          {{ dialogData.formData.url }}
        </el-descriptions-item>
        <el-descriptions-item :span="3">
          <template #label>
            <div class="cell-item">
              图片效果
            </div>
          </template>
          <el-image
              style="width: 100px; height: 100px"
              :src="dialogData.formData.url"
              :zoom-rate="1.2"
              :preview-src-list="[dialogData.formData.url]"
              :initial-index="0"
              fit="cover"
          />
        </el-descriptions-item>
      </el-descriptions>

      <template #footer>
          <span class="dialog-footer">
            <el-button @click="closeDialog">关闭</el-button>
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
