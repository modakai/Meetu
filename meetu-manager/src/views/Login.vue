<script setup>
import {reactive, ref} from "vue";
import {User, Lock} from "@element-plus/icons-vue";
import { ElMessage } from 'element-plus'
import {loginAdmin} from "@/api/userApi";
import {useUserStore} from "@/stores/user";
import router, {setRoutes} from "@/router";

const loginData = reactive({
  username: '',
  password: '',
  loginType: 'PC'
})
const rules = reactive({
  username: [
    { required: true, message: '账号不能为空', trigger: 'blur' },
  ],
  password: [
    { required: true, message: '密码不能为空', trigger: 'blur' },
    { min: 3, max: 16, message: '密码长度范围在3~16位之间', trigger: 'blur', required: true}
  ],
})

const ruleLogin = ref()

// 登入方法
const login = () => {
  ruleLogin.value.validate(valid => {
    if (valid) {
      loginAdmin(loginData).then(res => {
        if (res.code === '200') {
          let userInfo = res.data
          ElMessage.success(`欢迎 ${userInfo.username} 登入后台管理系统`)
          router.push("/")
          useUserStore().setManagerInfo(userInfo)
        } else {
          ElMessage.error(res.msg)
        }
      })
    } else {
      ElMessage.error('请注意规范')
    }
  })
}
</script>

<template>
  <div class="login-box">
    <div class="login-form-box">
      <el-form
          ref="ruleLogin"
          :model="loginData"
          :rules="rules"
          size="large"
          status-icon
          @keydown.enter="login"
      >
        <div class="tips">登 入</div>
        <el-form-item prop="username">
          <el-input v-model="loginData.username" placeholder="请输入账号" clearable :prefix-icon="User"/>
        </el-form-item>
        <el-form-item prop="password">
          <el-input v-model="loginData.password" show-password placeholder="请输入账号" clearable :prefix-icon="Lock"/>
        </el-form-item>
        <el-form-item>
          <el-button
              type="primary"
              style="width: 100%;
              font-size: 20px"
              @click.prevent="login"
          >
            Login
          </el-button>
        </el-form-item>

      </el-form>
    </div>
  </div>
</template>

<style scoped lang="less">
.login-box {
  height: 100vh;
  overflow: hidden;
  background: radial-gradient(circle at center, var(--el-fill-color-lighter), var(--el-bg-color-page));

  .login-form-box {
    width: 400px;
    margin: 150px auto;
    padding: 30px;
    background-color: #ffffff;
    border-radius: 10px;
    box-shadow: 0 0 10px rgba(0, 0, 0, 0.2);

    .tips {
      margin-bottom: 20px;
      text-align: center;
      color: dodgerblue;
      font-size: 30px;
      font-weight: bold;
    }
  }
}
</style>