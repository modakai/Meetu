<script setup>
import { RouterView } from "vue-router";
import { useUserStore } from "@/stores/user";
import {ref} from "vue";
import {logoutAdmin} from "@/api/userApi";
import {ElMessage} from "element-plus";

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
            :default-active="$route.path.replace('/', '')"
            :default-openeds="admin.menus.map(v => v.id + '')"
            class="el-menu-demo el-menu-vertical-demo"
            style="border: none"
            router
        >
          <div v-for="item in admin.menus" :key="item.id">
            <div v-if="item.type === 2">
              <el-menu-item :index="item.path" v-if="!item.hide">
                <el-icon v-if="item.icon">
                  <component :is="item.icon"></component>
                </el-icon>
                <span>{{ item.name }}</span>
              </el-menu-item>
            </div>
            <div v-else>
              <el-sub-menu :index="item.id + ''" v-if="!item.hide">
                <template #title>
                  <el-icon v-if="item.icon">
                    <component :is="item.icon"></component>
                  </el-icon>
                  <span>{{ item.name }}</span>
                </template>
                <div  v-for="subItem in item.children" :key="subItem.id">
                  <el-menu-item :index="subItem.path" v-if="!subItem.hide">
                    <template #title>
                      <el-icon v-if="subItem.icon">
                        <component :is="subItem.icon"></component>
                      </el-icon>
                      <span>{{ subItem.name }}</span>
                    </template>
                  </el-menu-item>
                </div>
              </el-sub-menu>
            </div>
          </div>
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