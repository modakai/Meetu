<script setup>
import {reactive, defineEmits, ref} from "vue";
import {ElMessage} from "element-plus";
import {useUserStore} from "@/stores/user";
import {modifyUser} from "@/api/userApi";

const userStore = useUserStore()
const user = userStore.getManagerInfo
const token = userStore.getAuthorization

const state = reactive({
  form: {}
})
state.form = Object.assign({}, user)
const handleImportSuccess = (res) => {
  state.form.avatar = res.data
  ElMessage.success("上传头像成功")
}

let $myEmit = defineEmits(['getAvatar'])
const avatarUploadUrl = ref('/api/file/upload')
const save = () => {

  modifyUser(state.form).then(res => {
    if (res.code === '200') {
      ElMessage.success('更新成功')
      userStore.setUser(res.data)
      $myEmit('getAvatar', res.data.avatar)
    } else {
      ElMessage.error(res.msg)
    }
  })
}
</script>

<template>
  <div>
    <el-card style="width: 40%; margin: 10px auto">
      <el-form style="width: 80%; margin: 0 auto" label-width="60px">
        <div style="text-align: center">
          <el-upload
              class="avatar-uploader"
              :show-file-list="false"
              :action='avatarUploadUrl'
              :on-success="handleImportSuccess"
              :headers="{ Authorization: token}"
          >
            <img v-if="state.form.avatar" :src="'http://localhost:8848'+ state.form.avatar" class="avatar" />
            <el-icon v-else class="avatar-uploader-icon"><Plus /></el-icon>
          </el-upload>
        </div>

        <el-form-item label="用户名" style="margin-top: 20px">
          <el-input v-model="state.form.username" disabled></el-input>
        </el-form-item>
        <el-form-item label="姓名">
          <el-input v-model="state.form.name"></el-input>
        </el-form-item>
        <el-form-item label="邮箱">
          <el-input v-model="state.form.email"></el-input>
        </el-form-item>
      </el-form>
      <div style="text-align: center; width: 100%">
        <el-button type="primary" @click="save">保存</el-button>
      </div>
    </el-card>
  </div>
</template>

<style scoped>
.avatar-uploader .avatar {
  width: 120px;
  height: 120px;
  display: block;
}
.avatar-uploader .el-upload {
  border: 1px dashed #ccc;
  border-radius: 50%;
  cursor: pointer;
  position: relative;
  overflow: hidden;
}

.avatar-uploader .el-upload:hover {
  border-color: #ccc;
}

.el-icon.avatar-uploader-icon {
  font-size: 28px;
  color: #8c939d;
  width: 120px;
  height: 120px;
  text-align: center;
}
</style>

