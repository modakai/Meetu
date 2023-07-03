<script>
  import request from "@/api/request";
  export default {
    name: 'Register',
    data() {
      var validatePass2 = (rule, value, callback) => {
        if (value === '') {
          callback(new Error('请再次输入密码'));
        } else if (value !== this.form.pass) {
          callback(new Error('两次输入密码不一致!'));
        } else {
          callback();
        }
      };
      return {
        form: {
          username: '',
          password: '',
          confirmPassword: '',
        },
        rules: {
          username: [
            { required: true, message: '请输入用户名', trigger: 'blur' },
            { min: 3, max: 5, message: '长度在 3 到 5 个字符', trigger: 'blur' }
          ],
          password: [
            { required: true, message: '密码不能为空', trigger: 'blur' },
          ],
          confirmPassword: [
            { validator: validatePass2, trigger: 'blur' },
          ],
        },
      }
    },
    methods: {
      // 登入
      login() {
        // 校验
        this.$refs['userForm'].validate((valid, obj) => {

          if (valid) {
            // 返回 true 则说明校验成功
            request.post("/register", this.form).then(res => {
              if (res.code === '200') {
                this.$message.success("注册成功")
                this.$router.push("/login")
              } else {
                this.$message.error(res.msg)
              }
            })
          } else {
            // 校验失败
            this.$message.error('不符合规范哦! 请注意规范');
          }
        })
      }
    }
  }
</script>

<template>
  <div class="outside-box">
    <div class="form-box">
      <el-form
          :model="form"
          ref="userForm"
          :rules="rules"
          status-icon
      >
        <h2 style="text-align: center; color: skyblue">注册</h2>
        <el-form-item prop="username">
          <el-input
              v-model="form.username"
              placeholder="请输入账号"
              prefix-icon="el-icon-user"
              status-icon
          >
          </el-input>
        </el-form-item>
        <el-form-item prop="password">
          <el-input
              v-model="form.password"
              show-password placeholder="请输入密码"
              autocomplete="new-password"
              prefix-icon="el-icon-lock"
              status-icon
          >
          </el-input>

        </el-form-item>
        <el-form-item prop="confirmPassword">

          <el-input
              v-model="form.confirmPassword"
              show-password placeholder="请输入密码"
              autocomplete="new-password"
              prefix-icon="el-icon-lock"
              status-icon
          >
          </el-input>

        </el-form-item>
        <div style="margin-bottom: 0.83em">
          <el-button style="width: 100%" type="primary" @click="login" @keyup.enter="login">注册</el-button>
        </div>
        <div style="text-align: right;">
          <el-button type="text" @click="$router.push('/register')">已有账号?&nbsp;立即登入</el-button>
        </div>
      </el-form>
    </div>
  </div>
</template>

<style scoped>
  .outside-box {
    height: 100vh;
    overflow: hidden;
    position: relative;
    background-image: linear-gradient(to top, #f3e7e9 0%, #e3eeff 99%, #e3eeff 100%);
  }

  .form-box {
    width: 420px;
    border-radius: 10px;
    margin: 0 auto;
    //background-image: linear-gradient(to top, #f3e7e9 0%, #e3eeff 99%, #e3eeff 100%);
    box-shadow: 0 0 25px -1px rgba(0, 0, 0, .2);
    padding: 20px;
    position: absolute;
    top: 50%;
    left: 50%;
    transform: translateX(-50%) translateY(-50%);
  }
</style>