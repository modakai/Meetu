<script>
  import request from "@/api/request";
  import { useUserStore } from "@/store/user";

  export default {
    name: 'Login',
    data() {
      return {
        form: {
          username: '',
          password: ''
        },
        rules: {
          username: [
            { required: true, message: '请输入账号或邮箱', trigger: 'blur' },
            // { min: 3, max: 5, message: '长度在 3 到 5 个字符', trigger: 'blur' }
          ],
          password: [
            { required: true, message: '请输入密码', trigger: 'blur' },
          ],
        },
        pwdVis: false,
        pwdForm: {
          email: '',
          code: ''
        },
        rulePwds: {
          email: [
            { required: true, message: '邮箱不能为空', trigger: 'blur' },
            { required: true, type: 'email', message: '邮箱格式不正确', trigger: 'blur'}
          ],
          code: [
            { required: true, message: '验证码不能为空', trigger: 'blur' },
          ],
        },
        time: 0,
        interval: -1,
        emailReg: /^\w+((.\w+)|(-\w+))@[A-Za-z0-9]+((.|-)[A-Za-z0-9]+).[A-Za-z0-9]+$/,
      }
    },
    methods: {
      // 登入
      login() {
        // 校验
        this.$refs['userForm'].validate((valid, obj) => {

          if (valid) {
            // 返回 true 则说明校验成功
            request.post("/login", this.form).then(res => {
              if (res.code === '200') {
                this.$message.success("登入成功")
                const store = useUserStore()
                // TODO 后期存储 TOKEN
                store.$patch({userInfo: res.data})

                this.$router.push("/")
              } else {
                this.$message.error(res.msg)
              }
            })
          } else {
            // 校验失败
            this.$message.error('不符合规范哦! 请注意规范');
          }
        })
      },
      // 发生验证码
      sendEmail() {
        if (!this.emailReg.test(this.pwdForm.email)) {
          this.$message.warning('请输入合法邮箱')
          this.$refs.rulePwdForm.validateField('email');
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
            type: "RESET_PASSWORD"
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
      },
      // 重置密码
      resetPwd() {
        request.put('/reset/password', {
          data: JSON.stringify(this.pwdForm)
        }).then(res => {
          if (res.code === '200') {
            // 这种方式并不友好, 建议使用 Element-ui的Steps 步骤条 组件实现
            // 先验证 邮箱身份, 验证成功则进行修改密码步骤
            // 验证邮箱失败(邮箱不存在的情况) 让用户继续停留在这个页面,并且给出对应的提示, 提供一个去注册的按钮
            this.$message.success("以为您重置密码! 密码为: " + res.data)
          } else if (res.code === '400') {
            this.$message.warning(res.msg)
          } else {
            this.$message.error("未知异常")
          }
        })
      },
      resetResetPwdDialog() {
        this.pwdForm = {
          email: '',
          code: ''
        }
        this.$refs.rulePwdForm.resetFields()
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

        <h2 style="text-align: center; color: skyblue">登录</h2>
        <el-form-item prop="username">

          <el-input
              v-model="form.username"
              placeholder="请输入账号或邮箱"
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
        <div style="margin-bottom: 0.83em">
          <el-button style="width: 100%" type="primary" @click="login" @keyup.enter="login">登录</el-button>
        </div>
        <div style="display: flex; justify-content: space-between">
          <el-button type="text" link @click.prevent="pwdVis = true">忘记密码</el-button>
          <el-button type="text" @click="$router.push('/register')">没有账号?请注册</el-button>
        </div>
      </el-form>
    </div>

    <el-dialog
        title="修改密码"
        :visible.sync="pwdVis"
        :destroy-on-close="true"
        @closed="resetResetPwdDialog"
        :close-on-click-modal="false"
    >
      <el-form :model="pwdForm" ref="rulePwdForm" :rules="rulePwds" status-icon>
        <el-form-item prop="email" label="邮箱" label-width="120px">
          <el-input
              v-model="pwdForm.email" autocomplete="off"
              prefix-icon="el-icon-message"
              clearable
              status-icon
          ></el-input>
        </el-form-item>
        <el-form-item prop="code" label="验证码" label-width="120px">
          <div style="display: flex">
            <el-input v-model="pwdForm.code" placeholder="请输入验证码" clearable style="flex: 1"></el-input>
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
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="pwdVis = false">取 消</el-button>
        <el-button type="primary" @click="resetPwd">确 认</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<style scoped>
  .outside-box {
    height: 100vh;
    overflow: hidden;
    position: relative;
    background-image: linear-gradient(to top, #a8edea 0%, #fed6e3 100%);
  }

  .form-box {
    width: 360px;
    border-radius: 10px;
    margin: 0 auto;
    background-image: linear-gradient(to top, #fff1eb 0%, #ace0f9 100%);
    box-shadow: 0 0 5px -2px rgba(0, 0, 0, .5);
    padding: 20px;
    position: absolute;
    top: 50%;
    left: 50%;
    transform: translateX(-50%) translateY(-50%);
  }
</style>