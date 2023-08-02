<script setup>
import { RouterView } from "vue-router";
import { useUserStore } from "@/stores/user";
import {ref} from "vue";
import {logoutAdmin} from "@/api/userApi";
import {ElMessage} from "element-plus";

import {
  Document,
  Menu as IconMenu,
  Location,
  Setting,
} from '@element-plus/icons-vue'
import router from "@/router";


const admin = ref(useUserStore().getManagerInfo)

const logout = () => {

  // 发送请求
  logoutAdmin({
    uid: admin.value.uid,
    loginType: 'PC'
  }).then(res => {
    if (res.code === '200') {
      // 重载 manager中的数据
      useUserStore().reset()
      // 跳转登入页
      router.push("/login")
      ElMessage.success('退出成功')
    } else {
      ElMessage.error(res.msg)
    }
  })


}

</script>

<template>
  <div class="layout">
    <!-- 头部 -->
    <div class="home-header">
      <div style="display: flex">
        <!-- LOGO -->
        <div class="logo">
          MeetU校园交友后台管理
        </div>

        <!--  管理员信息  -->
        <div class="user-info">
          <div class="avatar">

            <el-dropdown>
              <el-avatar
                  :size="40"
                  :src="admin.avatar"
              />
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item>个人信息</el-dropdown-item>
                  <el-dropdown-item @click="logout">退出系统</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </div>
        </div>
      </div>
    </div>

    <!--  body   -->
    <div style="display: flex">
      <aside class="home-aside">
        <el-menu
            router
            default-active="/"
            class="el-menu-vertical-demo"
        >
          <el-sub-menu index="1">
            <template #title>
              <el-icon>
                <location/>
              </el-icon>
              <span>Navigator One</span>
            </template>
            <el-menu-item index="/">item one</el-menu-item>
          </el-sub-menu>
          <el-menu-item index="2">
            <el-icon>
              <icon-menu/>
            </el-icon>
            <span>Navigator Two</span>
          </el-menu-item>
          <el-menu-item index="3" disabled>
            <el-icon>
              <document/>
            </el-icon>
            <span>Navigator Three</span>
          </el-menu-item>
          <el-menu-item index="4">
            <el-icon>
              <setting/>
            </el-icon>
            <span>Navigator Four</span>
          </el-menu-item>
        </el-menu>
      </aside>

      <div class="home-main">
        <RouterView />
      </div>
    </div>
  </div>
</template>

<style scoped lang="less">
.layout {
  // 头部
  .home-header {
    height: 60px;
    line-height: 60px;
    border-bottom: 1px solid #ccc;
    background-color: aliceblue;

    // 头部-左边
    .logo {
      width: 200px;
      height: 60px;
      color: dodgerblue;
      text-align: center;
      font-weight: bold;
      font-size: 20px;
    }

    // 头部 右边
    .user-info {
      display: flex;
      flex: 1;
      justify-content: end;
      align-items: center;
      height: 60px;

      .avatar {
        display: flex;
        justify-content: center;
        align-items: center;
        width: 80px;
        height: 60px;
      }

    }
  }

  // 侧边栏
  .home-aside {
    width: 200px;
    min-height: calc(100vh - 60px);
    border-right: 1px solid #ccc;
  }

  // 主体
  .home-main {
    flex: 1;
    padding: 15px;
  }
}
</style>