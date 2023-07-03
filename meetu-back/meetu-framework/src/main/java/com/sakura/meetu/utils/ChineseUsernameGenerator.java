package com.sakura.meetu.utils;

import java.util.Random;

/**
 * 随机生成昵称
 *
 * @author sakura
 */
public class ChineseUsernameGenerator {

    private static final String[] SURNAME = {
            "赵", "钱", "孙", "李", "周", "吴", "郑", "王", "冯", "陈",
            "褚", "卫", "蒋", "沈", "韩", "杨", "朱", "秦", "尤", "许",
            "何", "吕", "施", "张", "孔", "曹", "严", "华", "金", "魏",
            "陶", "姜", "戚", "谢", "邹", "喻", "柏", "水", "窦", "章",
            "云", "苏", "潘", "葛", "奚", "范", "彭", "郎", "鲁", "韦",
            "昌", "马", "苗", "凤", "花", "方", "俞", "任", "袁", "柳",
            "邓", "鲍", "史", "唐", "费", "廉", "岑", "薛", "雷", "贺",
            "倪", "汤", "滕", "殷", "罗", "毕", "郝", "邬", "安", "常",
            "乐", "于", "时", "傅", "皮", "卞", "齐", "康", "伍", "余",
            "元", "卜", "顾", "孟", "平", "黄", "和", "穆", "萧", "尹"
    };

    private static final String[] NAME = {
            "海", "明", "立", "强", "冬", "春", "夏", "秋", "庆", "光",
            "山", "林", "江", "国", "新", "建", "民", "品", "中", "华",
            "为", "忠", "学", "志", "玉", "德", "虎", "飞", "胜", "超",
            "正", "向", "东", "西", "南", "北", "宝", "荣", "全", "贵",
            "富", "顺", "智", "彩", "霞", "云", "星", "月", "龙", "凤",
            "金", "银", "石", "木", "水", "火", "土", "福", "禄", "寿",
            "宏", "祥", "吉", "顺", "安", "乐", "美", "悦", "逸", "馨",
            "青", "杏", "丽", "秀", "艳", "婷", "芳", "娜", "凤", "琳"
    };


    /**
     * 生成一个 2 到 nameLen 长度的中文昵称
     *
     * @param nameLen 要生成中文昵称的最大长度
     * @return 返回一个随机的中文昵称
     */
    public static String generateChineseUsername(int nameLen) {
        Random rand = new Random();
        String surname = SURNAME[rand.nextInt(SURNAME.length)];
        String name = "";
        int numChars = rand.nextInt(nameLen) + 2; // 随机生成1至5之间的数字
        for (int i = 0; i < numChars; i++) {
            name += NAME[rand.nextInt(NAME.length)];
        }
        return surname + name;
    }

    /**
     * 生成一个 2 到 5 长度的中文昵称
     *
     * @return 返回一个随机的中文昵称
     */
    public static String generateChineseUsername() {
        Random rand = new Random();
        String surname = SURNAME[rand.nextInt(SURNAME.length)];
        String name = "";
        int numChars = rand.nextInt(4) + 2; // 随机生成1至5之间的数字
        for (int i = 0; i < numChars; i++) {
            name += NAME[rand.nextInt(NAME.length)];
        }
        return surname + name;
    }
}