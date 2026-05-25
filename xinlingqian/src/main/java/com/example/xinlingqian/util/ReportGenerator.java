package com.example.xinlingqian.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class ReportGenerator {
    
    private static final Map<String, String> tagChineseNames = new HashMap<>();
    static {
        // 能量状态
        tagChineseNames.put("FULL", "满电怪");
        tagChineseNames.put("POWER", "强力输出");
        tagChineseNames.put("ZZZZ", "装死者");
        tagChineseNames.put("SLOW", "0.5倍速");
        tagChineseNames.put("BED", "床之眷者");
        tagChineseNames.put("MEH", "好累人");
        tagChineseNames.put("LOW", "电量过低");
        // 情绪基调
        tagChineseNames.put("SUN", "小太阳");
        tagChineseNames.put("CALM", "平静如镜");
        tagChineseNames.put("CHILL", "松弛感大师");
        tagChineseNames.put("FAKE", "伪人");
        tagChineseNames.put("BLUE", "淡淡的蓝");
        tagChineseNames.put("GRR", "暴躁辣椒");
        tagChineseNames.put("OUCH", "破碎感");
        // 社交状态
        tagChineseNames.put("MALO", "吗喽");
        tagChineseNames.put("HIDE", "电子乌龟");
        tagChineseNames.put("ATM-er", "送钱者");
        tagChineseNames.put("CHAT", "话唠精");
        tagChineseNames.put("BOSS", "领导者");
        tagChineseNames.put("JOKER", "小丑");
        tagChineseNames.put("HUG", "贴贴怪");
        // 内在思维
        tagChineseNames.put("FLOW", "心流者");
        tagChineseNames.put("OH-NO", "哦不人");
        tagChineseNames.put("WHEEL", "跑轮鼠");
        tagChineseNames.put("ZEN", "短暂佛");
        tagChineseNames.put("AHA", "顿悟者");
        tagChineseNames.put("FOG", "脑雾人");
        tagChineseNames.put("FOCUS", "激光眼");
        // 行为驱动
        tagChineseNames.put("YUM", "美食雷达");
        tagChineseNames.put("BUY", "野性消费");
        tagChineseNames.put("PLAY", "头号玩家");
        tagChineseNames.put("GOGO", "行者");
        tagChineseNames.put("CRY", "泪失禁");
        tagChineseNames.put("READ", "宇宙探索者");
        tagChineseNames.put("DO IT", "彻底疯狂");
        tagChineseNames.put("WILD", "野人");
    }
    
    public static String getChineseTagName(String tag) {
        return tagChineseNames.getOrDefault(tag, tag);
    }
    public static String determinePersonalityType(double ev, double me, double sc, double cc) {
        if (ev >= 4.0 && me >= 3.5) return "暖阳型 ☀️";
        if (ev >= 3.0 && ev <= 3.9 && me >= 3.0 && me <= 3.9 && sc >= 2.5 && sc <= 3.5) return "静水型 🌊";
        if (ev >= 2.0 && ev <= 3.0 && me >= 3.0 && cc >= 3.5) return "韧草型 🌱";
        if (sc <= 2.5 && me >= 3.0 && me <= 4.0) return "孤岛型 🏝️";
        if (cc <= 2.5 && me <= 2.5) return "迷雾型 🌫️";
        if (ev >= 2.0 && ev <= 3.0 && sc >= 3.5) return "星火型 ✨";
        return "原色型 🎨";
    }

    public static String getPersonalityDescription(String type) {
        Map<String, String> descriptions = new HashMap<>();
        descriptions.put("暖阳型 ☀️", "你的内心住着一个小太阳。不需要外界给予太多，你自己就能发光发热。这种温暖不是喧嚣的热闹，而是一种从心底泛起的、稳定的亮堂。你容易被小事打动，也愿意把这份暖意递给别人。请保护好这份珍贵的火光。");
        descriptions.put("静水型 🌊", "你的情绪世界像一面安静的湖。不好不坏，不悲不喜，有一种难得的稳定。你不需要每天都轰轰烈烈，平淡对你来说不是乏味，而是一种自在。偶尔有风吹过会有涟漪，但很快又恢复平静。这种稳定，是很多人羡慕却学不来的能力。");
        descriptions.put("韧草型 🌱", "你可能是那种\"看起来没事，其实心里装着事\"的人。情绪底色偏沉，但你有一副好用的头脑和不服输的韧劲。你会思考很多，有时想太多成了负担，但你总能找到办法把日子理顺。你不是不难过，你只是习惯了带着难过往前走。辛苦了，你真的挺厉害的。");
        descriptions.put("孤岛型 🏝️", "你是一座装备齐全的孤岛。自己发电、自己供水、自己处理一切。和人相处对你来说消耗大于补给，独处反而是充电。这不是孤僻，而是一种精密的自我保护。你享受自由，但也偶尔会在深夜感到一种淡淡的、说不清的孤单。没关系，岛和岛之间，海底的山脉其实是连着的。");
        descriptions.put("迷雾型 🌫️", "你最近可能觉得脑子里蒙着一层雾。事情想不清楚，决定做不出来，精力也跟不上。这感觉不剧烈，但像潮湿的天气一样，渗透到每一天里。这不是你的错，也不是你\"不够努力\"。脑子也需要除湿。给自己一点时间，雾会散的。");
        descriptions.put("星火型 ✨", "你习惯在人群中取暖。独处时情绪容易往下沉，但和人对上信号的那一刻，你会亮起来。你的情绪和人际关系紧密相连——被理解的时候就活过来，被忽视的时候就黯淡下去。这让你有时很累，但也说明你拥有温柔的天赋：你在意别人，也因为在意而善良。记得把这善良，也分一点给自己。");
        descriptions.put("原色型 🎨", "你的情绪世界是一幅层次丰富的画，单一的颜色无法定义你。有时明亮，有时灰暗，有时安静，有时躁动。这种复杂不是问题，而是你作为一个完整的人的正常纹理。从今天开始，慢慢认识自己的每一种颜色，都是你的一部分。");
        return descriptions.getOrDefault(type, "你的心灵底片类型正在生成中...");
    }

    public static String getMhiLevel(double mhi) {
        if (mhi >= 80) return "优秀 😊";
        if (mhi >= 65) return "良好 🙂";
        if (mhi >= 50) return "普通 😐";
        if (mhi >= 35) return "注意 🥺";
        return "预警 😢";
    }

    public static String getBaselineSuggestion(double ev, double me, double sc, double cc) {
        double min = Math.min(Math.min(ev, me), Math.min(sc, cc));
        if (ev == min) return "👂 试试这个：找一首曾经让你单曲循环过的老歌，闭上眼睛听一遍。不需要做什么，就让旋律带你回到那个有光的时刻。";
        if (me == min) return "🛌 试试这个：今天不给自己定任何目标。只做一件事——躺平。不是浪费时间，是在给你的精神充电。";
        if (sc == min) return "📱 试试这个：给你通讯录里一个很久没联系但想起来会觉得温暖的人，发一个简单的\"最近怎么样\"。不一定要聊很久，但连接本身就是意义。";
        return "📝 试试这个：拿出一张纸（或者备忘录），把你脑子里所有在转的事全部写下来——不用排序，不用解决，只是倒出来。让纸分担你的内存。";
    }

    public static String selectMainTag(double mhi, double ev, double me, double sc, double cc) {
        List<String> tags;
        if (mhi >= 80) {
            tags = Arrays.asList("SUN", "FULL", "FLOW", "GOGO", "POWER", "CHAT", "BOSS", "AHA", "FOCUS", "DO IT");
            if (me >= Math.max(Math.max(ev, sc), cc)) return "FULL";
            if (cc >= Math.max(Math.max(ev, me), sc)) return "FLOW";
            return tags.get(new Random().nextInt(tags.size()));
        } else if (mhi >= 65) {
            tags = Arrays.asList("CALM", "CHILL", "YUM", "PLAY", "READ", "CHAT", "ZEN", "HUG");
            return tags.get(new Random().nextInt(tags.size()));
        } else if (mhi >= 50) {
            tags = Arrays.asList("SLOW", "MEH", "WHEEL", "FOG", "HIDE", "ZZZZ", "BED", "FAKE");
            if (me < 2.5) return "SLOW";
            if (cc < 2.5) return "FOG";
            return tags.get(new Random().nextInt(tags.size()));
        } else if (mhi >= 35) {
            tags = Arrays.asList("BLUE", "LOW", "OUCH", "OH-NO", "WHEEL", "HIDE", "GRR", "CRY");
            if (ev < 2.5) return "BLUE";
            if (sc < 2.5) return "HIDE";
            return tags.get(new Random().nextInt(tags.size()));
        } else {
            tags = Arrays.asList("ZZZZ", "FAKE", "CRY", "OUCH", "OH-NO", "LOW", "FOG");
            return tags.get(new Random().nextInt(tags.size()));
        }
    }

    public static String getTagDescription(String tag) {
        Map<String, String> descriptions = new HashMap<>();
        descriptions.put("FULL", "今天的你像一块充满电的电池，精力充沛到无处安放。做什么都顺，想什么都快。这种状态很稀有，记得好好利用它——但也别一口气耗尽，明天还要用呢。");
        descriptions.put("POWER", "你今天进入了高效模式。事情一件接一件地完成，像开了挂一样。这种成就感是真实的营养，喂养着你的自信心。干得漂亮。");
        descriptions.put("SUN", "心里敞亮敞亮的，见谁都想笑。世界在你眼里加了柔光滤镜。你的存在本身，今天可能也在温暖着某些人。");
        descriptions.put("CALM", "内心的湖面纹丝不动。没人打扰，你也不打扰谁。这种宁静是很多人花钱都买不到的东西，请珍惜。");
        descriptions.put("CHILL", "火烧眉毛了？先喝杯茶再说。别人都在焦虑转圈，你在旁边翘着二郎腿。松弛感不是没有压力，而是选择不被压力牵着走。");
        descriptions.put("FLOW", "完全沉浸在手上的事里，忘了时间，也忘了自己。这种状态是最高级的快乐之一——你找到了和自己相处的最优方式。");
        descriptions.put("GOGO", "身体想动！想出门、想跑步、想感受风。你的身体在告诉你：我需要呼吸。回应它。");
        descriptions.put("CHAT", "嘴巴停不下来，分享欲爆棚。今天你的聊天记录可能比过去一周都多。太好了，你找到了愿意听你的人。");
        descriptions.put("SLOW", "今天的你在用0.5倍速生活。反应慢半拍，说话慢吞吞，世界请先暂停一会儿。这不是迟钝，是一种节电模式。");
        descriptions.put("MEH", "说不上很快乐，也谈不上很难过。就是一种\"好累\"。身体不累，但精神在打哈欠。今天可以不用那么用力地活着。");
        descriptions.put("WHEEL", "脑子在不停转，但好像永远跑不出那个轮子。想很多、想很深、但想不出答案。内耗进行时。也许今天可以先停下来，爬出轮子。");
        descriptions.put("FOG", "注意力无法集中，思维变得模糊。不是你不聪明，只是今天的雾有点厚。雾总会散的，在散之前慢慢走。");
        descriptions.put("HIDE", "社恐模式ON。今天只想缩回自己的壳里，消息免打扰，世界请安静。这个壳是你的安全区，没人能随便进来。");
        descriptions.put("BLUE", "心里下了一场毛毛雨，不大，但一直没停。说不出原因，也不需要原因。蓝色也是一种颜色，忧伤也可以是一种陪伴。");
        descriptions.put("LOW", "你今天的电量只剩下1%了。急需充电宝和一张床。请勿强行开机，请勿要求超频运行。今天是低功耗日。");
        descriptions.put("OUCH", "今天经历了一些事，心里有什么东西\"啪\"地一声，碎了。允许自己难过，也允许自己慢慢拼。碎过的拼图，也可以是另一种美。");
        descriptions.put("OH-NO", "脑子在上演灾难片。事情还没发生，已经崩溃了三轮。焦虑的小剧场上演了一天的循环剧。休息一下，你是编剧，可以改剧本。");
        descriptions.put("ZZZZ", "灵魂已关机，肉体还在硬撑。今天你可能觉得\"睁着眼睛\"已经是最大的成就了。没关系，有些日子就是用来\"熬过去\"的，不丢人。");
        descriptions.put("FAKE", "表面\"我很好\"，内心\"救救我\"。今天的你扮演了一整天的正常人，累了吧。回到只有自己的地方，你可以卸妆。");
        descriptions.put("CRY", "今天的泪点极低。一首歌、一句话就能让你鼻酸。泪水也是身体自我清洁的方式，流出来，就流出来。");
        descriptions.put("YUM", "今天的你，对美食毫无抵抗力。胃是心灵的B计划——心情好时用食物庆祝，心情不好时用食物弥补。");
        descriptions.put("PLAY", "今天属于虚拟世界。游戏、动漫、剧——能让你暂时逃离现实的，都是今天的好伙伴。");
        descriptions.put("READ", "求知欲爆棚，想在别人的故事里找到自己。今天适合泡书店、翻书页、让文字带你飞到别处。");
        descriptions.put("HUG", "今天渴望一个拥抱、一个倾听的耳朵、一个温暖的回应。想靠近、想被接纳。你值得被拥抱，也值得被爱。");
        descriptions.put("BOSS", "今天掌控全场！说什么都对，做什么都顺。你就是会议室里最靓的仔。");
        descriptions.put("AHA", "今天突然想通了一个道理，像一道光劈开了雾。通透了，舒坦了。记下这个顿悟，以后可以回来看看。");
        descriptions.put("FOCUS", "目标明确、路径清晰、执行力拉满。今天你不被任何东西分心，指哪打哪。这种专注力让人羡慕。");
        descriptions.put("DO IT", "想做一些平常不敢做的事！人生苦短，及时行乐。今天允许自己野一点。");
        descriptions.put("GRR", "易燃易爆炸。心里的烦躁值已经达到99%，随时可能爆。这不是你脾气坏，可能是今天承载了太多。找个安全的方式释放。");
        descriptions.put("BED", "今天你和床的关系非同一般。它一直在呼唤你，而你很难拒绝。床是你的安全区、充电站、避难所。今天是休息日，不是摆烂日。");
        descriptions.put("WILD", "想去公园里光脚踩草地，想爬一棵树，想和自然重新链接。5A级景区不如一片野草地。今天的你渴望挣脱束缚，回归本真。");
        return descriptions.getOrDefault(tag, "这个标签正在生成中...");
    }

    public static List<String> selectSubTags(double mhi, double ev, double me, double sc, double cc) {
        List<String> result = new ArrayList<>();
        Random random = new Random();

        Map<String, List<String>> evTags = new HashMap<>();
        evTags.put("high", Arrays.asList("SUN", "CALM", "YUM", "CHILL"));
        evTags.put("mid", Arrays.asList("MEH", "ZEN", "FAKE"));
        evTags.put("low", Arrays.asList("BLUE", "GRR", "OUCH", "CRY"));

        Map<String, List<String>> meTags = new HashMap<>();
        meTags.put("high", Arrays.asList("POWER", "GOGO", "FOCUS", "DO IT", "WILD"));
        meTags.put("mid", Arrays.asList("SLOW", "BED", "PLAY", "BUY"));
        meTags.put("low", Arrays.asList("ZZZZ", "LOW", "WHEEL"));

        Map<String, List<String>> scTags = new HashMap<>();
        scTags.put("high", Arrays.asList("CHAT", "HUG", "MALO", "BOSS"));
        scTags.put("mid", Arrays.asList("HIDE", "JOKER", "ATM-er"));
        scTags.put("low", Arrays.asList("HIDE", "OUCH", "FAKE"));

        Map<String, List<String>> ccTags = new HashMap<>();
        ccTags.put("high", Arrays.asList("FLOW", "AHA", "FOCUS", "READ"));
        ccTags.put("mid", Arrays.asList("FOG", "WHEEL", "ZEN"));
        ccTags.put("low", Arrays.asList("OH-NO", "FOG", "WHEEL"));

        List<String> evPool = evTags.get(getLevel(ev));
        List<String> mePool = meTags.get(getLevel(me));
        List<String> scPool = scTags.get(getLevel(sc));
        List<String> ccPool = ccTags.get(getLevel(cc));

        List<List<String>> pools = Arrays.asList(evPool, mePool, scPool, ccPool);
        Collections.shuffle(pools);

        for (List<String> pool : pools) {
            if (result.size() >= 3) break;
            String tag = pool.get(random.nextInt(pool.size()));
            if (!result.contains(tag)) {
                result.add(tag);
            }
        }

        return result;
    }

    public static String generateInsight(double mhi, double ev, double me, double sc, double cc) {
        if (mhi >= 80) {
            double maxDim = Math.max(Math.max(ev, me), Math.max(sc, cc));
            if (ev == maxDim) return "今天的你像一颗充满电的小太阳，自己发光还不够，还总想照到别人。这种状态很珍贵，记下来，以后阴天可以拿出来看看。";
            if (me == maxDim) return "今天效率拉满，事情一件接一件地完成，好像全世界都在给你让路。这种感觉真好，尽情享受这种\"什么都能搞定\"的爽快感。";
            if (sc == maxDim) return "今天你的社交能量满格，和人聊天像在充电。那些被你逗笑的人、和你聊到停不下来的人，都是你今天的能量来源。";
            return "今天思路清晰得像被擦过的玻璃，看什么都通透。趁着脑子好用，可以搞定一些需要深度思考的事。";
        } else if (mhi >= 65) {
            double maxDim = Math.max(Math.max(ev, me), Math.max(sc, cc));
            if (ev == maxDim) return "今天心情不错，没有大起大落，但有一种淡淡的轻松。这种\"不太用力\"的状态，其实是很高级的舒服。";
            if (me == maxDim) return "今天事情做得挺顺，该做的都做了，还有余力做一些小事犒劳自己。这种日子不轰烈，但踏实。";
            if (sc == maxDim) return "今天有人陪、有话说，没有惊天动地的故事，但有细水长流的连接。这种温度刚刚好。";
            return "今天脑子比较清楚，事情想得明白。这种掌控感让你今天过得有条有理。";
        } else if (mhi >= 50) {
            double minDim = Math.min(Math.min(ev, me), Math.min(sc, cc));
            if (me == minDim) return "今天的你有点\"省电模式\"的意思。事情虽然都做了，但总感觉在消耗意志力，每做一件事都想\"好累\"。给自己安排一个小憩吧，不必为此感到抱歉。";
            if (ev == minDim) return "今天的情绪像阴天的光线——不刺眼，但也没那么亮堂。说不上多难过，就是一种淡淡的\"不太想说话\"。没关系，阴天也是天气的一种。";
            if (sc == minDim) return "今天你似乎没怎么和人产生联系——可能是不想，也可能是没机会。偶尔缩回自己的壳里也没关系的，这是你的安全空间。";
            return "今天脑子有点迷糊，注意力不太好集中。可能忘了一些事，也可能花更多时间才想清楚。没关系，今天可以暂时放弃\"高效\"，允许自己走神。";
        } else if (mhi >= 35) {
            return "今天对你来说可能不太容易——情绪、精力、或者人际关系上似乎有些负担。如果你感到疲惫、低落或想躲起来，这不是软弱，这是身体在告诉你\"我需要被照顾\"。给自己一些更低门槛的善意：喝点热水、裹紧被子、允许自己今天不完美。你已经很好了。";
        } else {
            return "今天你可能过得很辛苦。或许有些事情让你感到沉重，或许什么都没发生但就是提不起劲。不想说话、不想动、不想面对——这些都没关系。你不需要今天就好起来。累的时候可以先停下来，在这个角落里安静地待一会儿。明天的事交给明天，今晚的你，只需要呼吸和休息。";
        }
    }

    public static String getDailySuggestion(double ev, double me, double sc, double cc) {
        double min = Math.min(Math.min(ev, me), Math.min(sc, cc));
        if (ev == min) return "👂 试试这个：找一首曾经让你单曲循环过的老歌，闭上眼睛听一遍。不需要做什么，就让旋律带你回到那个有光的时刻。";
        if (me == min) return "🛌 试试这个：今天不给自己定任何目标。只做一件事——躺平。不是浪费时间，是在给你的精神充电。";
        if (sc == min) return "📱 试试这个：给你通讯录里一个很久没联系但想起来会觉得温暖的人，发一个简单的\"最近怎么样\"。不一定要聊很久，但连接本身就是意义。";
        return "📝 试试这个：拿出一张纸（或者备忘录），把你脑子里所有在转的事全部写下来——不用排序，不用解决，只是倒出来。让纸分担你的内存。";
    }

    private static String getLevel(double score) {
        if (score > 3.5) return "high";
        if (score >= 2.5) return "mid";
        return "low";
    }
}
