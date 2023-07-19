<script setup>
import {
  Document,
  Menu as IconMenu,
  Location,
  Setting,
  Search,
} from '@element-plus/icons-vue'
import {ref} from "vue";

const name = ref('')

</script>

<template>
  <div class="home">
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
            <el-avatar :size="40" src="https://sakura-meetu.oss-cn-shenzhen.aliyuncs.com/2023/07/15/f8c36bb3f8124efd90e3e1815321c43fa.jpg" />
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
              <el-icon><location /></el-icon>
              <span>Navigator One</span>
            </template>
            <el-menu-item index="/">item one</el-menu-item>
          </el-sub-menu>
          <el-menu-item index="2">
            <el-icon><icon-menu /></el-icon>
            <span>Navigator Two</span>
          </el-menu-item>
          <el-menu-item index="3" disabled>
            <el-icon><document /></el-icon>
            <span>Navigator Three</span>
          </el-menu-item>
          <el-menu-item index="4">
            <el-icon><setting /></el-icon>
            <span>Navigator Four</span>
          </el-menu-item>
        </el-menu>
      </aside>

      <div class="home-main">
        <div class="main-search">
          <el-input v-model="name" placeholder="请输入名称"/>
          <el-button
            type="primary"
          >
            <el-icon style="vertical-align: middle">
              <Search />
            </el-icon>
            <span style="vertical-align: middle">搜索</span>
          </el-button>
        </div>

      </div>
    </div>
  </div>
</template>

<style lang="less" scoped>
  .home {
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

      // 搜索框
      .main-search {
        /deep/ .el-input {
          width: 300px;
        }
        /deep/ .el-button {
          margin-left: 10px;
        }

      }
    }
  }
</style>