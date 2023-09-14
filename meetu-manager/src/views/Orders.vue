<script setup>
import {nextTick, reactive, ref} from "vue";
import {delBatch, ordersPage, delOne, saveOrUpdate} from "@/api/ordersApi";
import {useUserStore} from "@/stores/user";
import {ElMessage} from "element-plus";
import {goodsAll} from "@/api/goodsApi";
import {getUserAll} from "@/api/userApi";
import {findAddressListByUserId} from "@/api/addressApi";

const name = ref('')
const pageNum = ref(1)
const pageSize = ref(5)
const total = ref(0)
const data = reactive({
  table: [],
  pageMenus: useUserStore().getPageMenus(),
  goodsOptions: [],
  userOptions: []
})
const loading = ref(true)
getUserAll().then(res => {
  data.userOptions = res.data
})
goodsAll().then(res => {
  data.goodsOptions = res.data
})
// 对话框
let dialogFormRef = ref()
const dialogData = reactive({
  dialogFormVisible: false,
  title: '',
  formData: {}
})
// TODO 修改自己修改的地方
const dialogRules = reactive({
  goodsId: [
    {required: true, message: '请选择商品', trigger: 'blur'},
  ],
  num: [
    {required: true, message: '请输入换个数量', trigger: 'blur'},
    { min: 1, max: 4, message: '不能超过4位数', trigger: 'blur'},
    { pattern: /(?:^[1-9]([0-9]+)?(?:\.[0-9]{1,2})?$)|(?:^(?:0){1}$)|(?:^[0-9]\.[0-9](?:[0-9])?$)/, message: "请不要输入负数", trigger: 'change'},
  ],
  time: [
    {required: true, message: '请选择时间', trigger: 'blur'},
  ],
  userId: [
    {required: true, message: '请选择用户', trigger: 'blur'},
  ],
  address: [
    {required: true, message: '请选择用户收货地址', trigger: 'blur'},
  ],
})

// 加载数据
const load = () => {
  ordersPage({
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
const searchorders = () => {
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
  dialogData.title = '新增订单'
  resetDialog({})
}
// 编辑用户信息 随便查看详情
const dialogEdit = (row) => {
  dialogData.title = '编辑订单'
  resetDialog(row)
}
// 重置 对话框
const resetDialog = (data) => {
  dialogData.dialogFormVisible = true
  nextTick(() => {
    dialogData.formData = JSON.parse(JSON.stringify(data))
    // dialogData.formData.num = 1
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

const importDataUrl = ref('/api/orders/import')
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
    ElMessage.success("导入订单成功!")
    load()
  } else {
    ElMessage.error(response.msg)
  }
}

// 导出数据
const exportAll = () => {
  window.open("http://localhost:8848/api/orders/export")
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
      saveOrUpdate('/orders', dialogData.formData).then(res => {
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

// 表单下拉框选择的商品对象
const selectedGoodsObj = ref({})

const addressOptions = ref([])
const selectUser = () => {
  // 发送请求去找用户对应的地址
  findAddressListByUserId(dialogData.formData.userId).then(res => {
    addressOptions.value = res.data
  })
}
const selectedGoods = () => {
  let score = selectedGoodsObj.value.score
  let goodsId = selectedGoodsObj.value.id

  if (dialogData.formData.num === undefined) {
    dialogData.formData.num = 1
  }
  dialogData.formData.score = dialogData.formData.num * score
  dialogData.formData.goodsId = goodsId
}


</script>

<template>
  <div class="home">

    <div class="main-search">
      <el-input v-model="name" placeholder="请输入订单用户的名称"/>
      <el-button
          type="primary"
          @click="searchorders"
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
          v-show="data.pageMenus.includes('orders.add')"
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
          v-show="data.pageMenus.includes('orders.export')"
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
          v-show="data.pageMenus.includes('orders.import')"
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
              v-show="data.pageMenus.includes('orders.deleteBatch')"
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
        <el-table-column prop="code" label="订单编号"></el-table-column>
        <el-table-column prop="goodsId" label="商品">
          <template #default="scope">
           <span v-if="data.goodsOptions">
             {{ data.goodsOptions.find(item => item.id === scope.row.goodsId).name}}
           </span>
          </template>
        </el-table-column>
        <el-table-column prop="userId" label="订单用户">
          <template #default="scope">
            <span v-if="data.userOptions">
              {{ data.userOptions.find(item => item.id === scope.row.userId).name }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="num" label="换购数量"></el-table-column>
        <el-table-column prop="time" label="换购时间"></el-table-column>
        <el-table-column prop="score" label="积分"></el-table-column>
        <el-table-column prop="address" label="地址信息"></el-table-column>


        <el-table-column label="操作" width="180">
          <template #default="scope">
            <el-button
                v-show="data.pageMenus.includes('orders.edit')"
                type="primary"
                @click="dialogEdit(scope.row)"
            >
              编辑
            </el-button>

            <el-popconfirm title="您确定要删除吗？" @confirm="del(scope.row)">
              <template #reference>
                <el-button
                    type="danger"
                    v-show="data.pageMenus.includes('orders.delete')"
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

        <el-form-item prop="goodsId" label="商品">
          <el-select clearable v-model="selectedGoodsObj" value-key="id" @change="selectedGoods"  placeholder="请选择" style="width: 100%">
            <el-option v-for="item in data.goodsOptions" :key="item.id" :label="item.name" :value="item"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item prop="num" label="换够数量">
          <el-input  v-model="dialogData.formData.num" @change="selectedGoods"  autocomplete="off"></el-input>
        </el-form-item>
        <el-form-item prop="time" label="换购时间">
          <el-date-picker style="width: 100%" v-model="dialogData.formData.time" type="datetime" value-format="YYYY-MM-DD HH:mm:ss" placeholder="选择日期时间"></el-date-picker>
        </el-form-item>
        <el-form-item prop="score" label="积分">
          <el-input disabled v-model="dialogData.formData.score" autocomplete="off"></el-input>
        </el-form-item>
        <el-form-item prop="userId" label="用户">
          <el-select clearable v-model="dialogData.formData.userId" placeholder="请选择" @change="selectUser" style="width: 100%">
            <el-option v-for="item in data.userOptions" :key="item.id" :label="item.name" :value="item.id"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item prop="address" label="地址信息">
          <el-select no-data-text="用户无地址数据" clearable v-model="dialogData.formData.address" placeholder="请选择" style="width: 100%">
            <el-option v-for="item in addressOptions" :key="item.id" :label="item.address" :value="item.address"></el-option>
          </el-select>
        </el-form-item>


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
