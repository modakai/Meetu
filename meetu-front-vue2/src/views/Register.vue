<script>
  import request from "@/api/request";
  import {useUserStore} from "@/store/user";
  export default {
    name: 'Register',
    data() {
      var validatePass2 = (rule, value, callback) => {
        if (value === '') {
          callback(new Error('请再次输入密码'));
        } else if (value !== this.form.password) {
          callback(new Error('两次输入密码不一致!'));
        } else {
          callback();
        }
      };
      return {
        emailReg: /^\w+((.\w+)|(-\w+))@[A-Za-z0-9]+((.|-)[A-Za-z0-9]+).[A-Za-z0-9]+$/,
        time: 0,
        interval: -1,
        form: {
          username: '',
          name: '',
          password: '',
          confirmPassword: '',
          email: '',
          code: '',
        },
        rules: {
          username: [
            { required: true, message: '请输入用户名', trigger: 'blur' },
            { min: 3, max: 16, message: '长度在 3 到 16 个字符', trigger: 'blur' }
          ],
          password: [
            { required: true, message: '密码不能为空', trigger: 'blur' },
          ],
          code: [
            { required: true, message: '验证码不能为空', trigger: 'blur' },
          ],
          confirmPassword: [
            { validator: validatePass2, trigger: 'blur' },
          ],
          email: [
            { required: true, message: '邮箱不能为空', trigger: 'blur' },
            { required: true, type: 'email', message: '邮箱格式不正确', trigger: 'blur'}
          ],
        },
      }
    },
    methods: {
      // 登入
      register() {
        // 校验
        this.$refs['userForm'].validate((valid, obj) => {
          if (valid) {
            // 返回 true 则说明校验成功
            request.post("/register", this.form).then(res => {
              if (res.code === '200') {
                this.$message.success("注册成功")
                const store = useUserStore()
                store.setUserInfo(res.data)
                this.$router.push("/")
              } else {
                this.$message.error(res.msg)
              }
            }).catch(res => {
              this.$message.error("未知异常")
            })
          } else {
            // 校验失败
            this.$message.error('不符合规范哦! 请注意规范');
          }
        })
      },
      sendEmail() {
        if (!this.emailReg.test(this.form.email)) {
          this.$message.warning('请输入合法邮箱')
          this.$refs.userForm.validateField('email');
          return
        }

        // 清空定时器
        if (this.interval >= 0) {
          clearInterval(this.interval)
        }
        this.time = 60
        this.interval = setInterval(() => {
          if (this.time > 0) {
            this.time--
          }
        }, 1000)

        request.get('/email', {
          params: {
            email: this.form.email,
            type: "REGISTER"
          }
        }).then(res => {
          if (res.code === '200') {

            this.$message.success("发送成功")
          } else if (res.code === '400') {
            this.$message.warning(res.msg)
          } else {
            this.$message.error("未知异常")
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
              show-password placeholder="请确认密码"
              autocomplete="new-password"
              prefix-icon="el-icon-lock"
              status-icon
          >
          </el-input>
        </el-form-item>
        <el-form-item prop="email">
          <el-input
              v-model="form.email"
              type="email"
              placeholder="请输入邮箱"
              prefix-icon="el-icon-message"
              status-icon
          >
          </el-input>
        </el-form-item>
        <el-form-item prop="code">
         <div style="display: flex">
           <el-input v-model="form.code" placeholder="请输入验证码" style="flex: 1"></el-input>
           <el-button
               type="primary"
               plain
               @click.prevent="sendEmail"
               :disabled="time > 0"
               style="width: 120px; margin-left: 5px"
           >
             点击发送 <span v-show="time"> ({{ time }}) </span>
           </el-button>
         </div>
        </el-form-item>
        <el-form-item prop="name">
          <el-input
              v-model="form.name"
              placeholder="请输入昵称"
              prefix-icon="el-icon-user"
              status-icon
          >
          </el-input>
        </el-form-item>
        <div style="margin-bottom: 0.83em">
          <el-button style="width: 100%" type="primary" @click="register" @keyup.enter="register">注册</el-button>
        </div>
        <div style="text-align: right;">
          <el-button type="text" @click="$router.push('/login')">已有账号?&nbsp;立即登入</el-button>
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
    width: 480px;
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