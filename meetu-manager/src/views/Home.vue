<script setup>
import {reactive} from "vue";
import { noticeReleaseList} from "@/api/noticeApi";
import { ElLoading } from 'element-plus'

const state = reactive({
  notice: []
})

const load = () => {
  noticeReleaseList().then(res => {
    state.notice = res.data
    let service = ElLoading.service({fullscreen: true, text: '正在加载'});
    service.close();
  })
}
load()
</script>

<template>
  <div>
    <el-row :gutter="10">
      <el-col :span="12">
        <el-card shadow="hover">
          <template #header>
            <div>
              <span>Meetu校园交友平台介绍</span>
            </div>
          </template>
          <div>
            <div style="margin: 10px 0">
              校园交友平台是专门为大学生提供社交的网络平台，它可以为大学生提供一个安全、便利的和高效的交友环境，对于校园文化的建设，和学生发展具有重要的意义。
            </div>

            <div  style="margin: 10px 0">
              <ol>
                <li>扩展社交圈子： 让大学生不受时间和地理的限制，扩大社交的圈子，结交更多志同道合的朋友，有共同兴趣爱好的人，帮助他们建立广泛和稳定的人脉关系。
                </li>
                <li>提高社交的能力：	校园交友平台不仅可以为学生提供进行自我展示的机会，也可以通过线上交流和线下活动等方式增强沟通和合作能力，提高团队协作和领导能力。</li>
                <li>促进校园文化建设：校园交友平台可以为大学生搭建一个多元、包容和积极向上的社交平台，推动校园文化建设，丰富校园文化内涵</li>
                <li>促进学习成长：校园交友平台还可以为学生提供一些学习资源、分享经验及学科知识等内容，丰富大学生活，帮助学生快速适应大学生活并促进学习成长。</li>
              </ol>
             </div>
            <div style="font-weight: bold">项目开发者：<b style="color: dodgerblue">莫达凯，彭艺，黄皓</b></div>
          </div>
        </el-card>
      </el-col>

      <el-col :span="12">
        <el-card shadow="hover">
          <template #header>
            <div>
              <span>技术栈</span>
            </div>
          </template>
          <div style="display: flex">
            <div style="flex: 1; padding: 0 20px; line-height: 30px">
              <h3 style="font-weight: bold">后端</h3>
              <div>SpringBoot2</div>
              <div>Mybatis-Plus</div>
              <div>Sa-Token</div>
              <div>JWT</div>
              <div>Mysql</div>
              <div>...</div>
            </div>
            <div style="flex: 1; padding: 0 20px;  line-height: 30px">
              <h3 style="font-weight: bold">前端</h3>
              <div>Vue2</div>
              <div>Element-ui</div>
              <div>Axios</div>
              <div>WangEditor</div>
              <div>...</div>
            </div>
          </div>
          <div style="margin-top: 10px; padding-top: 10px; border-top: 1px solid #cccccc">
            <div style="font-weight: bold">开源地址：<a target="_blank" href="https://github.com/modakai/Meetu" style="color: dodgerblue">Let‘s go GitHub</a></div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-divider />

    <div>
      <el-card style="width: 50%;  margin: 10px 0">
        <template #header>
          <div>
            <span>公告列表</span>
          </div>
        </template>
        <div v-if="state.notice.length">
          <el-collapse accordion>
          <el-collapse-item v-for="(item,index) in state.notice" :key="item.id" :name="'' + index">
            <template #title>
              <span style="font-size: 20px;">{{ item.name }}</span>
              <span style="margin-left: 10px">{{ item.createTime }}</span>
            </template>
            <div v-html="item.content"></div>
          </el-collapse-item>
        </el-collapse>
        </div>
        <div v-else>
          <el-empty :image-size="60" description="暂无数据" />
        </div>

      </el-card>
    </div>
  </div>
</template>

<style scoped lang="less">

</style>