/*
 * Copyright (C) 2011 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.gionee.mms.alphbetindex;

import android.text.TextUtils;
import android.util.Log;

import java.text.Collator;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
// Gionee lixiaohu 20120726 add for CR00658035 start
import android.os.SystemProperties;
// Gionee lixiaohu 20120726 add for CR00658035 end
/**
 * An object to convert Chinese character to its corresponding pinyin string. For characters with
 * multiple possible pinyin string, only one is selected according to collator. Polyphone is not
 * supported in this implementation. This class is implemented to achieve the best runtime
 * performance and minimum runtime resources with tolerable sacrifice of accuracy. This
 * implementation highly depends on zh_CN ICU collation data and must be always synchronized with
 * ICU.
 *
 * Currently this file is aligned to zh.txt in ICU 4.6
 */
public class HanziToPinyin {
    private static final String TAG = "HanziToPinyin";

    // Turn on this flag when we want to check internal data structure.
    private static final boolean DEBUG = false;
    
    // Gionee liuyanbo 2012-06-15 add for CR00624548 start
    public static final String SPLIT_STRING = "`!``!!!`!!`";
    // Gionee liuyanbo 2012-06-15 add for CR00624548 end
    // Gionee lixiaohu 20120726 add for CR00658035 start
    private static final boolean gnFLYflag = SystemProperties.get("ro.gn.oversea.custom").equals("RUSSIA_FLY"); 
    // Gionee lixiaohu 20120726 add for CR00658035 end

    /**
     * Unihans array. Each unihans is the first one within same pinyin. Use it to determine pinyin
     * for all ~20k unihans.
     */

    // GIONEE licheng Apr 18, 2012 add for CR00573811 start
    public static final char[] GN_UNIHANS = {
        '\u6c88', '\u961A', '\u4fde', '\u94ad', '\u65bc', '\u7fdf', '\u77bf', '\u513F',

        /*
         * "单", "曾", "贾", "呆", "丁", "大", "嗯", "益"
         */
        '\u5355', '\u66FE', '\u8D3E', '\u5446', '\u4E01', '\u5927', '\u55EF', '\u76CA',
        
        /*
         * "行", "头", "戴", "客", "长", "宜"
         */
        '\u884C', '\u5934', '\u6234', '\u5ba2', '\u957f', '\u5b9c',
        
        /*
         * “呵”, "说"
         */
        '\u5475','\u8BF4'
    };
    public static final byte[][] GN_PINYINS = {
        {83, 72, 69, 78, 0, 0}, {75, 65, 78, 0, 0, 0}, {89, 85, 0, 0, 0, 0},
        {68, 79, 85, 0, 0}, {89, 85, 0, 0, 0, 0}, {90, 72, 65, 73, 0, 0},
        {81, 85, 0, 0, 0, 0}, {69, 82, 0, 0, 0, 0},

        /*
         * "Shan", "Zeng", "Jia"，
         * "Dai", "Ding", "Da", 
         * "En" "Yi"
         */
        {83, 104, 97, 110, 0, 0}, {90, 101, 110, 103, 0, 0}, {74, 105, 97, 0, 0, 0}, 
        {68, 97, 105,  0, 0, 0}, {68, 105, 110, 103, 0, 0}, {68, 97,  0, 0, 0, 0},
        {69, 110,  0,  0, 0, 0}, {89, 105, 0,  0,  0, 0},
        
        /*
         * "Hang", "Tou", "Dai"
         * "Ke", "Chang"， "Yi"
         */
        {72, 97, 110, 103, 0, 0}, {84, 111, 117, 0, 0, 0}, {68, 97, 105,  0, 0, 0},
        {75, 101, 0,  0, 0, 0}, {67, 104, 97, 110, 103, 0}, {89, 105, 0,  0,  0, 0},
        
        /*
         * “He”, "Shuo"
         */
        {72, 101, 0, 0, 0, 0}, {83, 104, 117, 111, 0, 0}
    };
    // GIONEE licheng Apr 18, 2012 add for CR00573811 end
    
    public static final char[] UNIHANS = {
            '\u5475', '\u54ce', '\u5b89', '\u80ae', '\u51f9',
            '\u516b', '\u6300', '\u6273', '\u90a6', '\u5305', '\u5351', '\u5954', '\u4f3b',
            '\u5c44', '\u8fb9', '\u6807', '\u618b', '\u90a0', '\u69df', '\u7676', '\u5cec',
            '\u5693', '\u5a47', '\u98e1', '\u4ed3', '\u64cd', '\u518a', '\u5d7e', '\u564c',
            '\u53c9', '\u9497', '\u8fbf', '\u4f25', '\u6284', '\u8f66', '\u62bb', '\u67fd',
            '\u5403', '\u5145', '\u62bd', '\u51fa', '\u6b3b', '\u63e3', '\u5ddd', '\u75ae',
            '\u5439', '\u6776', '\u9034', '\u75b5', '\u5306', '\u51d1', '\u7c97', '\u6c46',
            '\u5d14', '\u90a8', '\u6413', '\u5491', '\u5927', '\u75b8', '\u5f53', '\u5200',
            '\u6dc2', '\u5f97', '\u6265', '\u706f', '\u6c10', '\u55f2', '\u7538', '\u5201',
            '\u7239', '\u4ec3', '\u4e1f', '\u4e1c', '\u5517', '\u561f', '\u5073', '\u5806',
            '\u9413', '\u591a', '\u5a40', '\u8bf6', '\u5940', '\u97a5', '\u800c', '\u53d1',
            '\u5e06', '\u65b9', '\u98de', '\u5206', '\u4e30', '\u8985', '\u4ecf', '\u7d11',
            '\u4f15', '\u65ee', '\u8be5', '\u7518', '\u5188', '\u768b', '\u6208', '\u7d66',
            '\u6839', '\u5e9a', '\u5de5', '\u52fe', '\u4f30', '\u74dc', '\u7f6b', '\u5173',
            '\u5149', '\u5f52', '\u886e', '\u5459', '\u54c8', '\u54b3', '\u9878', '\u82c0',
            '\u84bf', '\u8bc3', '\u9ed2', '\u62eb', '\u4ea8', '\u5677', '\u543d', '\u9f41',
            '\u5322', '\u82b1', '\u6000', '\u72bf', '\u5ddf', '\u7070', '\u660f', '\u5419',
            '\u4e0c', '\u52a0', '\u620b', '\u6c5f', '\u827d', '\u9636', '\u5dfe', '\u52a4',
            '\u5182', '\u52fc', '\u530a', '\u5a1f', '\u5658', '\u519b', '\u5494', '\u5f00',
            '\u520a', '\u95f6', '\u5c3b', '\u533c', '\u524b', '\u80af', '\u962c', '\u7a7a',
            '\u62a0', '\u5233', '\u5938', '\u84af', '\u5bbd', '\u5321', '\u4e8f', '\u5764',
            '\u6269', '\u5783', '\u6765', '\u5170', '\u5577', '\u635e', '\u4ec2', '\u52d2',
            '\u5844', '\u5215', '\u5006', '\u5941', '\u826f', '\u64a9', '\u5217', '\u62ce',
            '\u3007', '\u6e9c', '\u9f99', '\u779c', '\u565c', '\u5a08', '\u7567', '\u62a1',
            '\u7f57', '\u5463', '\u5988', '\u973e', '\u5ada', '\u9099', '\u732b', '\u9ebc',
            '\u6c92', '\u95e8', '\u753f', '\u54aa', '\u7720', '\u55b5', '\u54a9', '\u6c11',
            '\u540d', '\u8c2c', '\u6478', '\u54de', '\u6bea', '\u62cf', '\u5b7b', '\u56e1',
            '\u56ca', '\u5b6c', '\u8bb7', '\u9981', '\u6041', '\u80fd', '\u59ae', '\u62c8',
            '\u5b22', '\u9e1f', '\u634f', '\u60a8', '\u5b81', '\u599e', '\u519c', '\u7fba',
            '\u5974', '\u597b', '\u8650', '\u632a', '\u5594', '\u8bb4', '\u8db4', '\u62cd',
            '\u7705', '\u4e53', '\u629b', '\u5478', '\u55b7', '\u5309', '\u4e15', '\u504f',
            '\u527d', '\u6c15', '\u59d8', '\u4e52', '\u948b', '\u5256', '\u4ec6', '\u4e03',
            '\u6390', '\u5343', '\u545b', '\u6084', '\u767f', '\u4fb5', '\u9751', '\u909b',
            '\u4e18', '\u66f2', '\u5f2e', '\u7f3a', '\u590b', '\u5465', '\u7a63', '\u5a06',
            '\u60f9', '\u4eba', '\u6254', '\u65e5', '\u8338', '\u53b9', '\u5982', '\u5827',
            '\u6875', '\u95f0', '\u82e5', '\u4ee8', '\u6be2', '\u4e09', '\u6852', '\u63bb',
            '\u8272', '\u68ee', '\u50e7', '\u6740', '\u7b5b', '\u5c71', '\u4f24', '\u5f30',
            '\u5962', '\u7533', '\u5347', '\u5c38', '\u53ce', '\u4e66', '\u5237', '\u6454',
            '\u95e9', '\u53cc', '\u8c01', '\u542e', '\u5981', '\u53b6', '\u5fea', '\u635c',
            '\u82cf', '\u72fb', '\u590a', '\u5b59', '\u5506', '\u4ed6', '\u82d4', '\u574d',
            '\u94f4', '\u5932', '\u5fd1', '\u71a5', '\u5254', '\u5929', '\u4f7b', '\u5e16',
            '\u5385', '\u56f2', '\u5077', '\u92c0', '\u6e4d', '\u63a8', '\u541e', '\u6258',
            '\u6316', '\u6b6a', '\u5f2f', '\u5c2a', '\u5371', '\u586d', '\u7fc1', '\u631d',
            '\u5140', '\u5915', '\u867e', '\u4eda', '\u4e61', '\u7071', '\u4e9b', '\u5fc3',
            '\u661f', '\u51f6', '\u4f11', '\u65f4', '\u8f69', '\u75b6', '\u52cb', '\u4e2b',
            '\u6079', '\u592e', '\u5e7a', '\u8036', '\u4e00', '\u6b2d', '\u5e94', '\u54df',
            '\u4f63', '\u4f18', '\u625c', '\u9e22', '\u66f0', '\u6655', '\u531d', '\u707d',
            '\u7ccc', '\u7242', '\u50ae', '\u5219', '\u8d3c', '\u600e', '\u5897', '\u5412',
            '\u635a', '\u6cbe', '\u5f20', '\u948a', '\u8707', '\u8d1e', '\u4e89', '\u4e4b',
            '\u4e2d', '\u5dde', '\u6731', '\u6293', '\u8de9', '\u4e13', '\u5986', '\u96b9',
            '\u5b92', '\u5353', '\u5b5c', '\u5b97', '\u90b9', '\u79df', '\u94bb', '\u539c',
            '\u5c0a', '\u6628', };

    /**
     * Pinyin array. Each pinyin is corresponding to unihans of same offset in the unihans array.
     */
    public static final byte[][] PINYINS = {
            { 65, 0, 0, 0, 0, 0 }, { 65, 73, 0, 0, 0, 0 }, { 65, 78, 0, 0, 0, 0 },
            { 65, 78, 71, 0, 0, 0 }, { 65, 79, 0, 0, 0, 0 }, { 66, 65, 0, 0, 0, 0 },
            { 66, 65, 73, 0, 0, 0 }, { 66, 65, 78, 0, 0, 0 }, { 66, 65, 78, 71, 0, 0 },
            { 66, 65, 79, 0, 0, 0 }, { 66, 69, 73, 0, 0, 0 }, { 66, 69, 78, 0, 0, 0 },
            { 66, 69, 78, 71, 0, 0 }, { 66, 73, 0, 0, 0, 0 }, { 66, 73, 65, 78, 0, 0 },
            { 66, 73, 65, 79, 0, 0 }, { 66, 73, 69, 0, 0, 0 }, { 66, 73, 78, 0, 0, 0 },
            { 66, 73, 78, 71, 0, 0 }, { 66, 79, 0, 0, 0, 0 }, { 66, 85, 0, 0, 0, 0 },
            { 67, 65, 0, 0, 0, 0 }, { 67, 65, 73, 0, 0, 0 },
            { 67, 65, 78, 0, 0, 0 }, { 67, 65, 78, 71, 0, 0 }, { 67, 65, 79, 0, 0, 0 },
            { 67, 69, 0, 0, 0, 0 }, { 67, 69, 78, 0, 0, 0 }, { 67, 69, 78, 71, 0, 0 },
            { 67, 72, 65, 0, 0, 0 }, { 67, 72, 65, 73, 0, 0 }, { 67, 72, 65, 78, 0, 0 },
            { 67, 72, 65, 78, 71, 0 }, { 67, 72, 65, 79, 0, 0 }, { 67, 72, 69, 0, 0, 0 },
            { 67, 72, 69, 78, 0, 0 }, { 67, 72, 69, 78, 71, 0 }, { 67, 72, 73, 0, 0, 0 },
            { 67, 72, 79, 78, 71, 0 }, { 67, 72, 79, 85, 0, 0 }, { 67, 72, 85, 0, 0, 0 },
            { 67, 72, 85, 65, 0, 0 }, { 67, 72, 85, 65, 73, 0 }, { 67, 72, 85, 65, 78, 0 },
            { 67, 72, 85, 65, 78, 71 }, { 67, 72, 85, 73, 0, 0 }, { 67, 72, 85, 78, 0, 0 },
            { 67, 72, 85, 79, 0, 0 }, { 67, 73, 0, 0, 0, 0 }, { 67, 79, 78, 71, 0, 0 },
            { 67, 79, 85, 0, 0, 0 }, { 67, 85, 0, 0, 0, 0 }, { 67, 85, 65, 78, 0, 0 },
            { 67, 85, 73, 0, 0, 0 }, { 67, 85, 78, 0, 0, 0 }, { 67, 85, 79, 0, 0, 0 },
            { 68, 65, 0, 0, 0, 0 }, { 68, 65, 73, 0, 0, 0 }, { 68, 65, 78, 0, 0, 0 },
            { 68, 65, 78, 71, 0, 0 }, { 68, 65, 79, 0, 0, 0 }, { 68, 69, 0, 0, 0, 0 },
            { 68, 69, 73, 0, 0, 0 }, { 68, 69, 78, 0, 0, 0 }, { 68, 69, 78, 71, 0, 0 },
            { 68, 73, 0, 0, 0, 0 }, { 68, 73, 65, 0, 0, 0 }, { 68, 73, 65, 78, 0, 0 },
            { 68, 73, 65, 79, 0, 0 }, { 68, 73, 69, 0, 0, 0 }, { 68, 73, 78, 71, 0, 0 },
            { 68, 73, 85, 0, 0, 0 }, { 68, 79, 78, 71, 0, 0 }, { 68, 79, 85, 0, 0, 0 },
            { 68, 85, 0, 0, 0, 0 }, { 68, 85, 65, 78, 0, 0 }, { 68, 85, 73, 0, 0, 0 },
            { 68, 85, 78, 0, 0, 0 }, { 68, 85, 79, 0, 0, 0 }, { 69, 0, 0, 0, 0, 0 },
            { 69, 73, 0, 0, 0, 0 }, { 69, 78, 0, 0, 0, 0 }, { 69, 78, 71, 0, 0, 0 },
            { 69, 82, 0, 0, 0, 0 }, { 70, 65, 0, 0, 0, 0 }, { 70, 65, 78, 0, 0, 0 },
            { 70, 65, 78, 71, 0, 0 }, { 70, 69, 73, 0, 0, 0 }, { 70, 69, 78, 0, 0, 0 },
            { 70, 69, 78, 71, 0, 0 }, { 70, 73, 65, 79, 0, 0 }, { 70, 79, 0, 0, 0, 0 },
            { 70, 79, 85, 0, 0, 0 }, { 70, 85, 0, 0, 0, 0 }, { 71, 65, 0, 0, 0, 0 },
            { 71, 65, 73, 0, 0, 0 }, { 71, 65, 78, 0, 0, 0 }, { 71, 65, 78, 71, 0, 0 },
            { 71, 65, 79, 0, 0, 0 }, { 71, 69, 0, 0, 0, 0 }, { 71, 69, 73, 0, 0, 0 },
            { 71, 69, 78, 0, 0, 0 }, { 71, 69, 78, 71, 0, 0 }, { 71, 79, 78, 71, 0, 0 },
            { 71, 79, 85, 0, 0, 0 }, { 71, 85, 0, 0, 0, 0 }, { 71, 85, 65, 0, 0, 0 },
            { 71, 85, 65, 73, 0, 0 }, { 71, 85, 65, 78, 0, 0 }, { 71, 85, 65, 78, 71, 0 },
            { 71, 85, 73, 0, 0, 0 }, { 71, 85, 78, 0, 0, 0 }, { 71, 85, 79, 0, 0, 0 },
            { 72, 65, 0, 0, 0, 0 }, { 72, 65, 73, 0, 0, 0 }, { 72, 65, 78, 0, 0, 0 },
            { 72, 65, 78, 71, 0, 0 }, { 72, 65, 79, 0, 0, 0 }, { 72, 69, 0, 0, 0, 0 },
            { 72, 69, 73, 0, 0, 0 }, { 72, 69, 78, 0, 0, 0 }, { 72, 69, 78, 71, 0, 0 },
            { 72, 77, 0, 0, 0, 0 }, { 72, 79, 78, 71, 0, 0 }, { 72, 79, 85, 0, 0, 0 },
            { 72, 85, 0, 0, 0, 0 }, { 72, 85, 65, 0, 0, 0 }, { 72, 85, 65, 73, 0, 0 },
            { 72, 85, 65, 78, 0, 0 }, { 72, 85, 65, 78, 71, 0 }, { 72, 85, 73, 0, 0, 0 },
            { 72, 85, 78, 0, 0, 0 }, { 72, 85, 79, 0, 0, 0 }, { 74, 73, 0, 0, 0, 0 },
            { 74, 73, 65, 0, 0, 0 }, { 74, 73, 65, 78, 0, 0 }, { 74, 73, 65, 78, 71, 0 },
            { 74, 73, 65, 79, 0, 0 }, { 74, 73, 69, 0, 0, 0 }, { 74, 73, 78, 0, 0, 0 },
            { 74, 73, 78, 71, 0, 0 }, { 74, 73, 79, 78, 71, 0 }, { 74, 73, 85, 0, 0, 0 },
            { 74, 85, 0, 0, 0, 0 }, { 74, 85, 65, 78, 0, 0 }, { 74, 85, 69, 0, 0, 0 },
            { 74, 85, 78, 0, 0, 0 }, { 75, 65, 0, 0, 0, 0 }, { 75, 65, 73, 0, 0, 0 },
            { 75, 65, 78, 0, 0, 0 }, { 75, 65, 78, 71, 0, 0 }, { 75, 65, 79, 0, 0, 0 },
            { 75, 69, 0, 0, 0, 0 }, { 75, 69, 73, 0, 0, 0 }, { 75, 69, 78, 0, 0, 0 },
            { 75, 69, 78, 71, 0, 0 }, { 75, 79, 78, 71, 0, 0 }, { 75, 79, 85, 0, 0, 0 },
            { 75, 85, 0, 0, 0, 0 }, { 75, 85, 65, 0, 0, 0 }, { 75, 85, 65, 73, 0, 0 },
            { 75, 85, 65, 78, 0, 0 }, { 75, 85, 65, 78, 71, 0 }, { 75, 85, 73, 0, 0, 0 },
            { 75, 85, 78, 0, 0, 0 }, { 75, 85, 79, 0, 0, 0 }, { 76, 65, 0, 0, 0, 0 },
            { 76, 65, 73, 0, 0, 0 }, { 76, 65, 78, 0, 0, 0 }, { 76, 65, 78, 71, 0, 0 },
            { 76, 65, 79, 0, 0, 0 }, { 76, 69, 0, 0, 0, 0 }, { 76, 69, 73, 0, 0, 0 },
            { 76, 69, 78, 71, 0, 0 }, { 76, 73, 0, 0, 0, 0 }, { 76, 73, 65, 0, 0, 0 },
            { 76, 73, 65, 78, 0, 0 }, { 76, 73, 65, 78, 71, 0 }, { 76, 73, 65, 79, 0, 0 },
            { 76, 73, 69, 0, 0, 0 }, { 76, 73, 78, 0, 0, 0 }, { 76, 73, 78, 71, 0, 0 },
            { 76, 73, 85, 0, 0, 0 }, { 76, 79, 78, 71, 0, 0 }, { 76, 79, 85, 0, 0, 0 },
            { 76, 86, 0, 0, 0, 0 }, { 76, 85, 65, 78, 0, 0 }, { 76, 85, 69, 0, 0, 0 },
            { 76, 85, 78, 0, 0, 0 }, { 76, 85, 79, 0, 0, 0 }, { 77, 0, 0, 0, 0, 0 },
            { 77, 65, 0, 0, 0, 0 }, { 77, 65, 73, 0, 0, 0 }, { 77, 65, 78, 0, 0, 0 },
            { 77, 65, 78, 71, 0, 0 }, { 77, 65, 79, 0, 0, 0 }, { 77, 69, 0, 0, 0, 0 },
            { 77, 69, 73, 0, 0, 0 }, { 77, 69, 78, 0, 0, 0 }, { 77, 69, 78, 71, 0, 0 },
            { 77, 73, 0, 0, 0, 0 }, { 77, 73, 65, 78, 0, 0 }, { 77, 73, 65, 79, 0, 0 },
            { 77, 73, 69, 0, 0, 0 }, { 77, 73, 78, 0, 0, 0 }, { 77, 73, 78, 71, 0, 0 },
            { 77, 73, 85, 0, 0, 0 }, { 77, 79, 0, 0, 0, 0 }, { 77, 79, 85, 0, 0, 0 },
            { 77, 85, 0, 0, 0, 0 }, { 78, 65, 0, 0, 0, 0 }, { 78, 65, 73, 0, 0, 0 },
            { 78, 65, 78, 0, 0, 0 }, { 78, 65, 78, 71, 0, 0 }, { 78, 65, 79, 0, 0, 0 },
            { 78, 69, 0, 0, 0, 0 }, { 78, 69, 73, 0, 0, 0 }, { 78, 69, 78, 0, 0, 0 },
            { 78, 69, 78, 71, 0, 0 }, { 78, 73, 0, 0, 0, 0 }, { 78, 73, 65, 78, 0, 0 },
            { 78, 73, 65, 78, 71, 0 }, { 78, 73, 65, 79, 0, 0 }, { 78, 73, 69, 0, 0, 0 },
            { 78, 73, 78, 0, 0, 0 }, { 78, 73, 78, 71, 0, 0 }, { 78, 73, 85, 0, 0, 0 },
            { 78, 79, 78, 71, 0, 0 }, { 78, 79, 85, 0, 0, 0 }, { 78, 85, 0, 0, 0, 0 },
            { 78, 85, 65, 78, 0, 0 }, { 78, 85, 69, 0, 0, 0 }, { 78, 85, 79, 0, 0, 0 },
            { 79, 0, 0, 0, 0, 0 }, { 79, 85, 0, 0, 0, 0 }, { 80, 65, 0, 0, 0, 0 },
            { 80, 65, 73, 0, 0, 0 }, { 80, 65, 78, 0, 0, 0 }, { 80, 65, 78, 71, 0, 0 },
            { 80, 65, 79, 0, 0, 0 }, { 80, 69, 73, 0, 0, 0 }, { 80, 69, 78, 0, 0, 0 },
            { 80, 69, 78, 71, 0, 0 }, { 80, 73, 0, 0, 0, 0 }, { 80, 73, 65, 78, 0, 0 },
            { 80, 73, 65, 79, 0, 0 }, { 80, 73, 69, 0, 0, 0 }, { 80, 73, 78, 0, 0, 0 },
            { 80, 73, 78, 71, 0, 0 }, { 80, 79, 0, 0, 0, 0 }, { 80, 79, 85, 0, 0, 0 },
            { 80, 85, 0, 0, 0, 0 }, { 81, 73, 0, 0, 0, 0 }, { 81, 73, 65, 0, 0, 0 },
            { 81, 73, 65, 78, 0, 0 }, { 81, 73, 65, 78, 71, 0 }, { 81, 73, 65, 79, 0, 0 },
            { 81, 73, 69, 0, 0, 0 }, { 81, 73, 78, 0, 0, 0 }, { 81, 73, 78, 71, 0, 0 },
            { 81, 73, 79, 78, 71, 0 }, { 81, 73, 85, 0, 0, 0 }, { 81, 85, 0, 0, 0, 0 },
            { 81, 85, 65, 78, 0, 0 }, { 81, 85, 69, 0, 0, 0 }, { 81, 85, 78, 0, 0, 0 },
            { 82, 65, 78, 0, 0, 0 }, { 82, 65, 78, 71, 0, 0 }, { 82, 65, 79, 0, 0, 0 },
            { 82, 69, 0, 0, 0, 0 }, { 82, 69, 78, 0, 0, 0 }, { 82, 69, 78, 71, 0, 0 },
            { 82, 73, 0, 0, 0, 0 }, { 82, 79, 78, 71, 0, 0 }, { 82, 79, 85, 0, 0, 0 },
            { 82, 85, 0, 0, 0, 0 }, { 82, 85, 65, 78, 0, 0 }, { 82, 85, 73, 0, 0, 0 },
            { 82, 85, 78, 0, 0, 0 }, { 82, 85, 79, 0, 0, 0 }, { 83, 65, 0, 0, 0, 0 },
            { 83, 65, 73, 0, 0, 0 }, { 83, 65, 78, 0, 0, 0 }, { 83, 65, 78, 71, 0, 0 },
            { 83, 65, 79, 0, 0, 0 }, { 83, 69, 0, 0, 0, 0 }, { 83, 69, 78, 0, 0, 0 },
            { 83, 69, 78, 71, 0, 0 }, { 83, 72, 65, 0, 0, 0 }, { 83, 72, 65, 73, 0, 0 },
            { 83, 72, 65, 78, 0, 0 }, { 83, 72, 65, 78, 71, 0 }, { 83, 72, 65, 79, 0, 0 },
            { 83, 72, 69, 0, 0, 0 }, { 83, 72, 69, 78, 0, 0 }, { 83, 72, 69, 78, 71, 0 },
            { 83, 72, 73, 0, 0, 0 }, { 83, 72, 79, 85, 0, 0 }, { 83, 72, 85, 0, 0, 0 },
            { 83, 72, 85, 65, 0, 0 }, { 83, 72, 85, 65, 73, 0 }, { 83, 72, 85, 65, 78, 0 },
            { 83, 72, 85, 65, 78, 71 }, { 83, 72, 85, 73, 0, 0 }, { 83, 72, 85, 78, 0, 0 },
            { 83, 72, 85, 79, 0, 0 }, { 83, 73, 0, 0, 0, 0 }, { 83, 79, 78, 71, 0, 0 },
            { 83, 79, 85, 0, 0, 0 }, { 83, 85, 0, 0, 0, 0 }, { 83, 85, 65, 78, 0, 0 },
            { 83, 85, 73, 0, 0, 0 }, { 83, 85, 78, 0, 0, 0 }, { 83, 85, 79, 0, 0, 0 },
            { 84, 65, 0, 0, 0, 0 }, { 84, 65, 73, 0, 0, 0 }, { 84, 65, 78, 0, 0, 0 },
            { 84, 65, 78, 71, 0, 0 }, { 84, 65, 79, 0, 0, 0 }, { 84, 69, 0, 0, 0, 0 },
            { 84, 69, 78, 71, 0, 0 }, { 84, 73, 0, 0, 0, 0 }, { 84, 73, 65, 78, 0, 0 },
            { 84, 73, 65, 79, 0, 0 }, { 84, 73, 69, 0, 0, 0 }, { 84, 73, 78, 71, 0, 0 },
            { 84, 79, 78, 71, 0, 0 }, { 84, 79, 85, 0, 0, 0 }, { 84, 85, 0, 0, 0, 0 },
            { 84, 85, 65, 78, 0, 0 }, { 84, 85, 73, 0, 0, 0 }, { 84, 85, 78, 0, 0, 0 },
            { 84, 85, 79, 0, 0, 0 }, { 87, 65, 0, 0, 0, 0 }, { 87, 65, 73, 0, 0, 0 },
            { 87, 65, 78, 0, 0, 0 }, { 87, 65, 78, 71, 0, 0 }, { 87, 69, 73, 0, 0, 0 },
            { 87, 69, 78, 0, 0, 0 }, { 87, 69, 78, 71, 0, 0 }, { 87, 79, 0, 0, 0, 0 },
            { 87, 85, 0, 0, 0, 0 }, { 88, 73, 0, 0, 0, 0 }, { 88, 73, 65, 0, 0, 0 },
            { 88, 73, 65, 78, 0, 0 }, { 88, 73, 65, 78, 71, 0 }, { 88, 73, 65, 79, 0, 0 },
            { 88, 73, 69, 0, 0, 0 }, { 88, 73, 78, 0, 0, 0 }, { 88, 73, 78, 71, 0, 0 },
            { 88, 73, 79, 78, 71, 0 }, { 88, 73, 85, 0, 0, 0 }, { 88, 85, 0, 0, 0, 0 },
            { 88, 85, 65, 78, 0, 0 }, { 88, 85, 69, 0, 0, 0 }, { 88, 85, 78, 0, 0, 0 },
            { 89, 65, 0, 0, 0, 0 }, { 89, 65, 78, 0, 0, 0 }, { 89, 65, 78, 71, 0, 0 },
            { 89, 65, 79, 0, 0, 0 }, { 89, 69, 0, 0, 0, 0 }, { 89, 73, 0, 0, 0, 0 },
            { 89, 73, 78, 0, 0, 0 }, { 89, 73, 78, 71, 0, 0 }, { 89, 79, 0, 0, 0, 0 },
            { 89, 79, 78, 71, 0, 0 }, { 89, 79, 85, 0, 0, 0 }, { 89, 85, 0, 0, 0, 0 },
            { 89, 85, 65, 78, 0, 0 }, { 89, 85, 69, 0, 0, 0 }, { 89, 85, 78, 0, 0, 0 },
            { 90, 65, 0, 0, 0, 0 }, { 90, 65, 73, 0, 0, 0 }, { 90, 65, 78, 0, 0, 0 },
            { 90, 65, 78, 71, 0, 0 }, { 90, 65, 79, 0, 0, 0 }, { 90, 69, 0, 0, 0, 0 },
            { 90, 69, 73, 0, 0, 0 }, { 90, 69, 78, 0, 0, 0 }, { 90, 69, 78, 71, 0, 0 },
            { 90, 72, 65, 0, 0, 0 }, { 90, 72, 65, 73, 0, 0 }, { 90, 72, 65, 78, 0, 0 },
            { 90, 72, 65, 78, 71, 0 }, { 90, 72, 65, 79, 0, 0 }, { 90, 72, 69, 0, 0, 0 },
            { 90, 72, 69, 78, 0, 0 }, { 90, 72, 69, 78, 71, 0 }, { 90, 72, 73, 0, 0, 0 },
            { 90, 72, 79, 78, 71, 0 }, { 90, 72, 79, 85, 0, 0 }, { 90, 72, 85, 0, 0, 0 },
            { 90, 72, 85, 65, 0, 0 }, { 90, 72, 85, 65, 73, 0 }, { 90, 72, 85, 65, 78, 0 },
            { 90, 72, 85, 65, 78, 71 }, { 90, 72, 85, 73, 0, 0 }, { 90, 72, 85, 78, 0, 0 },
            { 90, 72, 85, 79, 0, 0 }, { 90, 73, 0, 0, 0, 0 }, { 90, 79, 78, 71, 0, 0 },
            { 90, 79, 85, 0, 0, 0 }, { 90, 85, 0, 0, 0, 0 }, { 90, 85, 65, 78, 0, 0 },
            { 90, 85, 73, 0, 0, 0 }, { 90, 85, 78, 0, 0, 0 }, { 90, 85, 79, 0, 0, 0 }, };

    // Gionee lixiaohu 20120726 add for CR00658035 start
    private static final int [][] russian_array = {
                                           {1040,1072},
                                           {1041,1073},
                                           {1042,1074},
                                           {1043,1075},
                                           {1044,1076},
                                           {1045,1077},
                                           {1046,1078},
                                           {1047,1079},
                                           {1048,1080},
                                           {1049,1081},
                                           {1050,1082},
                                           {1051,1083},
                                           {1052,1084},
                                           {1053,1085},
                                           {1054,1086},
                                           {1055,1087},
                                           {1056,1088},
                                           {1057,1089},
                                           {1058,1090},
                                           {1059,1091},
                                           {1060,1092},
                                           {1061,1093},
                                           {1062,1094},
                                           {1063,1095},
                                           {1064,1096},
                                           {1065,1097},
                                           {1066,1098},
                                           {1067,1099},
                                           {1068,1100},
                                           {1069,1101},
                                           {1070,1102},
                                           {1071,1103},
                                          };
    // Gionee lixiaohu 20120726 add for CR00658035 end                                                  

    /** First and last Chinese character with known Pinyin according to zh collation */
    private static final String FIRST_PINYIN_UNIHAN = "\u963F";
    private static final String LAST_PINYIN_UNIHAN = "\u84D9";
    /** The first Chinese character in Unicode block */
    private static final char FIRST_UNIHAN = '\u3400';
    private static final Collator COLLATOR = Collator.getInstance(Locale.CHINA);

    private static HanziToPinyin sInstance;
    private final boolean mHasChinaCollator;

    public static class Token {
        /**
         * Separator between target string for each source char
         */
        public static final String SEPARATOR = " ";

        public static final int LATIN = 1;
        public static final int PINYIN = 2;
        public static final int UNKNOWN = 3;

        public Token() {
        }

        public Token(int type, String source, String target) {
            this.type = type;
            this.source = source;
            this.target = target;
        }

        /**
         * Type of this token, ASCII, PINYIN or UNKNOWN.
         */
        public int type;
        /**
         * Original string before translation.
         */
        public String source;
        /**
         * Translated string of source. For Han, target is corresponding Pinyin. Otherwise target is
         * original string in source.
         */
        public String target;
    }

    protected HanziToPinyin(boolean hasChinaCollator) {
        mHasChinaCollator = hasChinaCollator;
    }

    public static HanziToPinyin getInstance() {
        synchronized (HanziToPinyin.class) {
            if (sInstance != null) {
                return sInstance;
            }
            // Check if zh_CN collation data is available
            final Locale locale[] = Collator.getAvailableLocales();
            for (int i = 0; i < locale.length; i++) {
                if (locale[i].equals(Locale.CHINA)) {
                    // Do self validation just once.
                    if (DEBUG) {
                        Log.d(TAG, "Self validation. Result: " + doSelfValidation());
                    }
                    sInstance = new HanziToPinyin(true);
                    return sInstance;
                }
            }
            Log.w(TAG, "There is no Chinese collator, HanziToPinyin is disabled");
            sInstance = new HanziToPinyin(false);
            return sInstance;
        }
    }

    /**
     * Validate if our internal table has some wrong value.
     *
     * @return true when the table looks correct.
     */
    private static boolean doSelfValidation() {
        char lastChar = UNIHANS[0];
        String lastString = Character.toString(lastChar);
        for (char c : UNIHANS) {
            if (lastChar == c) {
                continue;
            }
            final String curString = Character.toString(c);
            int cmp = COLLATOR.compare(lastString, curString);
            if (cmp >= 0) {
                Log.e(TAG, "Internal error in Unihan table. " + "The last string \"" + lastString
                        + "\" is greater than current string \"" + curString + "\".");
                return false;
            }
            lastString = curString;
        }
        return true;
    }

    // Gionee liuyanbo 20120422 merge for CR00576798 start
    private Token getToken(char character, boolean flag) {
    // Gionee liuyanbo 20120422 merge for CR00576798 end
        Token token = new Token();
        final String letter = Character.toString(character);
        token.source = letter;
        int offset = -1;
        int cmp;
        if (character < 256) {
            token.type = Token.LATIN;
            token.target = letter;
            return token;
        } else if (character < FIRST_UNIHAN) {
            token.type = Token.UNKNOWN;
            token.target = letter;
            return token;
        } else {
            cmp = COLLATOR.compare(letter, FIRST_PINYIN_UNIHAN);
            if (cmp < 0) {
                token.type = Token.UNKNOWN;
                token.target = letter;
                return token;
            } else if (cmp == 0) {
                token.type = Token.PINYIN;
                offset = 0;
            } else {
                cmp = COLLATOR.compare(letter, LAST_PINYIN_UNIHAN);
                if (cmp > 0) {
                    token.type = Token.UNKNOWN;
                    token.target = letter;
                    return token;
                } else if (cmp == 0) {
                    token.type = Token.PINYIN;
                    offset = UNIHANS.length - 1;
                }
            }
        }

        token.type = Token.PINYIN;

        // GIONEE licheng Apr 18, 2012 add for CR00573811  start
        for (int i = 0; i < GN_UNIHANS.length; i++) {
            if (0 == COLLATOR.compare(letter, Character.toString(GN_UNIHANS[i]))) {
                StringBuilder pinyin = new StringBuilder();
                for (int j = 0; j < GN_PINYINS[i].length && GN_PINYINS[i][j] != 0; j++) {
                    pinyin.append((char) GN_PINYINS[i][j]);
                }
                token.target = pinyin.toString();
                return token;
            }
        }
        // GIONEE licheng Apr 18, 2012 add for CR00573811 end

        if (offset < 0) {
            int begin = 0;
            int end = UNIHANS.length - 1;
            while (begin <= end) {
                offset = (begin + end) / 2;
                final String unihan = Character.toString(UNIHANS[offset]);
                cmp = COLLATOR.compare(letter, unihan);
                if (cmp == 0) {
                    break;
                } else if (cmp > 0) {
                    begin = offset + 1;
                } else {
                    end = offset - 1;
                }
            }
        }
        if (cmp < 0) {
            offset--;
        }
        StringBuilder pinyin = new StringBuilder();
        for (int j = 0; j < PINYINS[offset].length && PINYINS[offset][j] != 0; j++) {
            pinyin.append((char) PINYINS[offset][j]);
        }
        // Gionee liuyanbo 20120422 merge for CR00576798 start
        //特殊处理单a,o,e汉字
        if (flag) {
            if (pinyin.length() == 1) {
                pinyin.append('0');
            }
        }
        // Gionee liuyanbo 20120422 merge for CR00576798 end
        token.target = pinyin.toString();
        return token;
    }

    /**
     * Convert the input to a array of tokens. The sequence of ASCII or Unknown characters without
     * space will be put into a Token, One Hanzi character which has pinyin will be treated as a
     * Token. If these is no China collator, the empty token array is returned.
     */
    public ArrayList<Token> get(final String input) {
        ArrayList<Token> tokens = new ArrayList<Token>();
        if (!mHasChinaCollator || TextUtils.isEmpty(input)) {
            // return empty tokens.
            return tokens;
        }
        final int inputLength = input.length();
        final StringBuilder sb = new StringBuilder();
        int tokenType = Token.LATIN;
        // Go through the input, create a new token when
        // a. Token type changed
        // b. Get the Pinyin of current charater.
        // c. current character is space.
        for (int i = 0; i < inputLength; i++) {
            final char character = input.charAt(i);
            if (character == ' ') {
                if (sb.length() > 0) {
                    addToken(sb, tokens, tokenType);
                }
            } else if (character < 256) {
                if (tokenType != Token.LATIN && sb.length() > 0) {
                    addToken(sb, tokens, tokenType);
                }
                tokenType = Token.LATIN;
                sb.append(character);
            } else if (character < FIRST_UNIHAN) {
                if (tokenType != Token.UNKNOWN && sb.length() > 0) {
                    addToken(sb, tokens, tokenType);
                }
                tokenType = Token.UNKNOWN;
                sb.append(character);
            } else {
                // Gionee liuyanbo 20120422 merge for CR00576798 start
                Token t = getToken(character, false);
                // Gionee liuyanbo 20120422 merge for CR00576798 end
                if (t.type == Token.PINYIN) {
                    if (sb.length() > 0) {
                        addToken(sb, tokens, tokenType);
                    }
                    tokens.add(t);
                    tokenType = Token.PINYIN;
                } else {
                    if (tokenType != t.type && sb.length() > 0) {
                        addToken(sb, tokens, tokenType);
                    }
                    tokenType = t.type;
                    sb.append(character);
                }
            }
        }
        if (sb.length() > 0) {
            addToken(sb, tokens, tokenType);
        }
        return tokens;
    }

    private void addToken(
            final StringBuilder sb, final ArrayList<Token> tokens, final int tokenType) {
        String str = sb.toString();
        tokens.add(new Token(tokenType, str, str));
        sb.setLength(0);
    }
    

    //The fillowing lines are provided and maintained by Mediatek inc.
    private class DialerSearchToken extends Token {
        static final int FIRSTCASE = 0;
        static final int UPPERCASE = 1;
        static final int LOWERCASE = 2;
    }
    
    public String getTokensForDialerSearch(final String input, StringBuilder offsets){
        
        if(offsets == null || input == null || TextUtils.isEmpty(input)){
            // return empty tokens
            return null;
        }
        
        StringBuilder subStrSet = new StringBuilder();
        ArrayList<Token> tokens = new ArrayList<Token>();
        ArrayList<String> shortSubStrOffset = new ArrayList<String>();
        final int inputLength = input.length();
        final StringBuilder subString = new StringBuilder();
        final StringBuilder subStrOffset = new StringBuilder();
        int tokenType = Token.LATIN;
        int caseTypePre = DialerSearchToken.FIRSTCASE;
        int caseTypeCurr = DialerSearchToken.UPPERCASE;
        int mPos = 0;
        
        // Go through the input, create a new token when
        // a. Token type changed
        // b. Get the Pinyin of current charater.
        // c. current character is space.
        // d. Token case changed from lower case to upper case, 
        // e. the first character is always a separated one
        // f character == '+' || character == '#' || character == '*' || character == ',' || character == ';'
        for (int i = 0; i < inputLength; i++) {
            final char character = input.charAt(i);
            if (character == '-' || character == ',' ){
                mPos++;
            } else if (character == ' ') {
                if (subString.length() > 0) {
                    addToken(subString, tokens, tokenType);
                    addOffsets(subStrOffset, shortSubStrOffset);
                }
                addSubString(tokens,shortSubStrOffset,subStrSet,offsets);
                mPos++;
                caseTypePre = DialerSearchToken.FIRSTCASE;
            } else if (character < 256) {
                if (tokenType != Token.LATIN && subString.length() > 0) {
                    addToken(subString, tokens, tokenType);
                    addOffsets(subStrOffset, shortSubStrOffset);
                 }
                   caseTypeCurr = (character>='A' && character<='Z')?DialerSearchToken.UPPERCASE:DialerSearchToken.LOWERCASE;
                   if(caseTypePre == DialerSearchToken.LOWERCASE && caseTypeCurr == DialerSearchToken.UPPERCASE){
                       addToken(subString, tokens, tokenType);
                       addOffsets(subStrOffset, shortSubStrOffset);
                   }
                   caseTypePre = caseTypeCurr;    
                tokenType = Token.LATIN;
                // Gionee lixiaohu 20120726 modify for CR00658035 start
                Character c;
                if (gnFLYflag) { 
                    c = dialerKeyMap.get(character);
                } else {
                    c = Character.toUpperCase(character);
                }
                // Gionee lixiaohu 20120726 modify for CR00658035 end
                if(c != null){
                    subString.append(c);
                    subStrOffset.append((char)mPos);
                }
                mPos++;
            } else if (character < FIRST_UNIHAN) {
               // Gionee lixiaohu 20120726 add for CR00658035 start
               if (gnFLYflag) { 
                   //Comment out. Do not cover unknown characters SINCE they can not be input. 
                    if (tokenType != Token.UNKNOWN && subString.length() > 0) {
                        addToken(subString, tokens, tokenType);
                        addOffsets(subStrOffset, shortSubStrOffset);
                        caseTypePre = DialerSearchToken.FIRSTCASE;
                    }
                    tokenType = Token.UNKNOWN;
                    Character c = dialerKeyMap.get(character);
                    if(c != null){
                        subString.append(c);
                        subStrOffset.append((char)(mPos));
                    }
                }
                // Gionee lixiaohu 20120726 add for CR00658035 end
                mPos++;
            } else {
                // GIONEE liuyanbo 2012-05-29 modified for CR00601070 start
                // Token t = getToken(character, true);
                Token t = getToken(character, false);
                // GIONEE liuyanbo 2012-05-29 modified for CR00601070 end
                int tokenSize = t.target.length();
                //Current type is PINYIN
                if (t.type == Token.PINYIN) {
                    if (subString.length() > 0) {
                        addToken(subString, tokens, tokenType);
                        addOffsets(subStrOffset, shortSubStrOffset);
                    }
                    tokens.add(t);
                    for(int j=0; j < tokenSize;j++)
                        subStrOffset.append((char)mPos);
                    addOffsets(subStrOffset,shortSubStrOffset);
                    tokenType = Token.PINYIN;
                    caseTypePre = DialerSearchToken.FIRSTCASE;
                    mPos++;
                } else {
                    // Gionee lixiaohu 20120726 add for CR00658035 start
                    if (gnFLYflag) { 
                        //Comment out. Do not cover special characters SINCE they can not be input.
                        if (tokenType != t.type && subString.length() > 0) {
                            addToken(subString, tokens, tokenType);
                            addOffsets(subStrOffset, shortSubStrOffset);
                            caseTypePre = DialerSearchToken.FIRSTCASE;
                        }else{
                            caseTypeCurr = (character>='A' && character<='Z')?DialerSearchToken.UPPERCASE:DialerSearchToken.LOWERCASE;
                            if(caseTypePre == DialerSearchToken.LOWERCASE && caseTypeCurr == DialerSearchToken.UPPERCASE){
                                addToken(subString, tokens, tokenType);
                                addOffsets(subStrOffset, shortSubStrOffset);
                            }
                            caseTypePre = caseTypeCurr;    
                        }
                        tokenType = t.type;
                        Character c = dialerKeyMap.get(character);
                        if(c != null){
                            subString.append(c);
                            subStrOffset.append(mPos);
                        }
                    }
                    // Gionee lixiaohu 20120726 add for CR00658035 end
                    mPos++;
                }
            }
            //IF the name string is too long, cut it off to meet the storage request of dialer search.
            if(mPos > 127)
                break;
        }
        if (subString.length() > 0) {
            addToken(subString, tokens, tokenType);
            addOffsets(subStrOffset, shortSubStrOffset);
        }
        addSubString(tokens,shortSubStrOffset,subStrSet,offsets);
        return subStrSet.toString();
    }
    
    
    // Gionee liuyanbo 20120422 merge for CR00576798 start
    /*
     * @hide
     */
    public String getFristCharsForDialerSearch(final String input){
        
        if(input == null || TextUtils.isEmpty(input)){
            // return empty tokens
            return null;
        }
        
        StringBuilder returnStr = new StringBuilder();
        StringBuilder subStrSet = new StringBuilder();
        ArrayList<Token> tokens = new ArrayList<Token>();
        ArrayList<String> shortSubStrOffset = new ArrayList<String>();
        final int inputLength = input.length();
        final StringBuilder subString = new StringBuilder();
        final StringBuilder subStrOffset = new StringBuilder();
        int tokenType = Token.LATIN;
        int caseTypePre = DialerSearchToken.FIRSTCASE;
        int caseTypeCurr = DialerSearchToken.UPPERCASE;
        int mPos = 0;
        
        // Go through the input, create a new token when
        // a. Token type changed
        // b. Get the Pinyin of current charater.
        // c. current character is space.
        // d. Token case changed from lower case to upper case, 
        // e. the first character is always a separated one
        // f character == '+' || character == '#' || character == '*' || character == ',' || character == ';'
        for (int i = 0; i < inputLength; i++) {
            final char character = input.charAt(i);
            if (character == '-' || character == ',' ){
                mPos++;
            } else if (character == ' ') {
                if (subString.length() > 0) {
                    addToken(subString, tokens, tokenType);
                    addOffsets(subStrOffset, shortSubStrOffset);
                }
                mPos++;
                caseTypePre = DialerSearchToken.FIRSTCASE;
            } else if (character < 256) {
                if (tokenType != Token.LATIN && subString.length() > 0) {
                    addToken(subString, tokens, tokenType);
                    addOffsets(subStrOffset, shortSubStrOffset);
                 }
                caseTypeCurr = (character>='A' && character<='Z')?DialerSearchToken.UPPERCASE:DialerSearchToken.LOWERCASE;
                if(caseTypePre == DialerSearchToken.LOWERCASE && caseTypeCurr == DialerSearchToken.UPPERCASE){
                    addToken(subString, tokens, tokenType);
                    addOffsets(subStrOffset, shortSubStrOffset);
                }
                caseTypePre = caseTypeCurr; 
                tokenType = Token.LATIN;
                //Character c = Character.toUpperCase(character);
                Character c = dialerKeyMap.get(character);                
                if(c != null){
                    subString.append(c);
                    subStrOffset.append((char)mPos);
                }
                mPos++;
            } else if (character < FIRST_UNIHAN) {
               // Gionee lixiaohu 20120726 add for CR00658035 start
               if (gnFLYflag) { 
                   if (tokenType != Token.UNKNOWN && subString.length() > 0) {
                        addToken(subString, tokens, tokenType);
                        addOffsets(subStrOffset, shortSubStrOffset);
                        caseTypePre = DialerSearchToken.FIRSTCASE;
                    }
                    tokenType = Token.UNKNOWN;
                    Character c = dialerKeyMap.get(character);
                    if(c != null){
                        subString.append(c);
                        subStrOffset.append((char)mPos);
                    }
                }
                // Gionee lixiaohu 20120726 add for CR00658035 end        
                mPos++;
            } else {
                Token t = getToken(character, true);
                int tokenSize = t.target.length();
                //Current type is PINYIN
                if (t.type == Token.PINYIN) {
                    if (subString.length() > 0) {
                        addToken(subString, tokens, tokenType);
                        addOffsets(subStrOffset, shortSubStrOffset);
                    }
                    tokens.add(t);
                    for(int j=0; j < tokenSize;j++)
                        subStrOffset.append((char)mPos);
                    addOffsets(subStrOffset,shortSubStrOffset);
                    tokenType = Token.PINYIN;
                    caseTypePre = DialerSearchToken.FIRSTCASE;
                    mPos++;
                } else { 
                    // Gionee lixiaohu 20120726 add for CR00658035 start 
                    if (gnFLYflag) {    
                        if (tokenType != t.type && subString.length() > 0) {
                            addToken(subString, tokens, tokenType);
                            addOffsets(subStrOffset, shortSubStrOffset);
                            caseTypePre = DialerSearchToken.FIRSTCASE;
                        }else{
                            caseTypeCurr = (character>='A' && character<='Z')?DialerSearchToken.UPPERCASE:DialerSearchToken.LOWERCASE;
                            if(caseTypePre == DialerSearchToken.LOWERCASE && caseTypeCurr == DialerSearchToken.UPPERCASE){
                                addToken(subString, tokens, tokenType);
                                addOffsets(subStrOffset, shortSubStrOffset);
                            }
                            caseTypePre = caseTypeCurr; 
                        }
                        tokenType = t.type;
                        Character c = dialerKeyMap.get(character);
                        if(c != null){
                            subString.append(c);
                            subStrOffset.append((char)mPos);
                        }
                    }    
                    // Gionee lixiaohu 20120726 add for CR00658035 end        
                    mPos++;
                }
            }
            //IF the name string is too long, cut it off to meet the storage request of dialer search.
            if(mPos > 127)
                break;
        }
        if (subString.length() > 0) {
            addToken(subString, tokens, tokenType); 
        }
        for (Token mtoken : tokens) {
            if (mtoken != null && mtoken.target != null) {
                if (mtoken.target.length() > 0) {
                    if (mtoken.type == Token.PINYIN) {
                        returnStr.append(mtoken.target.substring(0, 1));
                    } else {
                        returnStr.append(mtoken.target);
                    }
                }
            }
        }
        // Gionee liuyanbo 2012-06-15 add for CR00624548 start
        returnStr.append(SPLIT_STRING);
        // Gionee liuyanbo 2012-06-15 add for CR00624548 end
        for (Token mtoken : tokens) {
            if (mtoken != null && mtoken.target != null) {
                if (mtoken.type == Token.PINYIN) {
                    returnStr.append(mtoken.target).append(",");
                } else {
                    String targetStr = mtoken.target;
                    StringBuilder tempStr = new StringBuilder();
                    for (int i = 0; i < targetStr.length(); i++) {
                        tempStr.append(targetStr.charAt(i)).append(",");
                    } 
                    if (tempStr.length() > 0) {
                        tempStr.deleteCharAt(tempStr.length() - 1);
                    }
                    returnStr.append(tempStr).append(","); 
                }
            }
        }
        for (int i = 0; i < returnStr.length(); i++) {
            returnStr.replace(i, i + 1, "" + dialerKeyMap.get(returnStr.charAt(i)));
        }
        
        return returnStr.deleteCharAt(returnStr.length() - 1).toString().replaceAll("0", "");
    }
    // Gionee liuyanbo 20120422 merge for CR00576798 end
    
    private void addOffsets(final StringBuilder sb, final ArrayList<String> shortSubStrOffset){
        String str = sb.toString();
        shortSubStrOffset.add(str);
        sb.setLength(0);
    }
    
    private void addSubString(final ArrayList<Token> tokens, final ArrayList<String> shortSubStrOffset,
                            StringBuilder subStrSet, StringBuilder offsets){
        if(tokens == null || tokens.isEmpty())
            return;
        
        int size = tokens.size();
        int len = 0;
        StringBuilder mShortSubStr = new StringBuilder();
        StringBuilder mShortSubStrOffsets = new StringBuilder();
        StringBuilder mShortSubStrSet = new StringBuilder();
        StringBuilder mShortSubStrOffsetsSet = new StringBuilder();
        
        for(int i=size-1; i>=0 ; i--){
            String mTempStr = tokens.get(i).target;
            // Gionee liuyanbo 20120422 merge for CR00576798 start
            if (i == 0 && mTempStr.length() == 1 && tokens.get(0).type == Token.LATIN) {
                mTempStr = mTempStr + "0";
            }
         // Gionee liuyanbo 20120422 merge for CR00576798 end
            len += mTempStr.length();
            String mTempOffset = shortSubStrOffset.get(i);
            if(mShortSubStr.length()>0){
                mShortSubStr.deleteCharAt(0);
                mShortSubStrOffsets.deleteCharAt(0);
            }
            mShortSubStr.insert(0, mTempStr);
            mShortSubStr.insert(0,(char)len);
            mShortSubStrOffsets.insert(0,mTempOffset);
            mShortSubStrOffsets.insert(0,(char)len);
            mShortSubStrSet.insert(0,mShortSubStr);
            mShortSubStrOffsetsSet.insert(0, mShortSubStrOffsets);
        }
        
        subStrSet.append(mShortSubStrSet);
        offsets.append(mShortSubStrOffsetsSet);
        tokens.clear();
        shortSubStrOffset.clear();
    }
    //The previous lines are provided and maintained by Mediatek inc.    
    
    // Gionee liuyanbo 20120422 merge for CR00576798 end
    private static final HashMap<Character, Character> dialerKeyMap; 
    static {
        dialerKeyMap = new HashMap<Character, Character>();
        dialerKeyMap.put('0', '0');
        dialerKeyMap.put('1', '1');
        dialerKeyMap.put('2', '2');
        dialerKeyMap.put('a', '2');
        dialerKeyMap.put('b', '2');
        dialerKeyMap.put('c', '2');
        dialerKeyMap.put('A', '2');
        dialerKeyMap.put('B', '2');
        dialerKeyMap.put('C', '2');
        dialerKeyMap.put('3', '3');
        dialerKeyMap.put('d', '3');
        dialerKeyMap.put('e', '3');
        dialerKeyMap.put('f', '3');
        dialerKeyMap.put('D', '3');
        dialerKeyMap.put('E', '3');
        dialerKeyMap.put('F', '3');
        dialerKeyMap.put('4', '4');
        dialerKeyMap.put('g', '4');
        dialerKeyMap.put('h', '4');
        dialerKeyMap.put('i', '4');
        dialerKeyMap.put('G', '4');
        dialerKeyMap.put('H', '4');
        dialerKeyMap.put('I', '4');
        dialerKeyMap.put('5', '5');
        dialerKeyMap.put('j', '5');
        dialerKeyMap.put('k', '5');
        dialerKeyMap.put('l', '5');
        dialerKeyMap.put('J', '5');
        dialerKeyMap.put('K', '5');
        dialerKeyMap.put('L', '5');
        dialerKeyMap.put('6', '6');
        dialerKeyMap.put('m', '6');
        dialerKeyMap.put('n', '6');
        dialerKeyMap.put('o', '6');
        dialerKeyMap.put('M', '6');
        dialerKeyMap.put('N', '6');
        dialerKeyMap.put('O', '6');
        dialerKeyMap.put('7', '7');
        dialerKeyMap.put('p', '7');
        dialerKeyMap.put('q', '7');
        dialerKeyMap.put('r', '7');
        dialerKeyMap.put('s', '7');
        dialerKeyMap.put('P', '7');
        dialerKeyMap.put('Q', '7');
        dialerKeyMap.put('R', '7');
        dialerKeyMap.put('S', '7');
        dialerKeyMap.put('8', '8');
        dialerKeyMap.put('t', '8');
        dialerKeyMap.put('u', '8');
        dialerKeyMap.put('v', '8');
        dialerKeyMap.put('T', '8');
        dialerKeyMap.put('U', '8');
        dialerKeyMap.put('V', '8');
        dialerKeyMap.put('9', '9');
        dialerKeyMap.put('w', '9');    
        dialerKeyMap.put('x', '9');    
        dialerKeyMap.put('y', '9');    
        dialerKeyMap.put('z', '9');
        dialerKeyMap.put('W', '9');    
        dialerKeyMap.put('X', '9');    
        dialerKeyMap.put('Y', '9');    
        dialerKeyMap.put('Z', '9');
        dialerKeyMap.put('#', '#');
        dialerKeyMap.put('*', '*');
        dialerKeyMap.put('+', '+');
        dialerKeyMap.put(',', ',');
        dialerKeyMap.put(';', ';');
        // Gionee liuyanbo 2012-06-15 add for CR00624548 start
        dialerKeyMap.put('`', '`');
        dialerKeyMap.put('!', '!');
        // Gionee liuyanbo 2012-06-15 add for CR00624548 end
        // Gionee lixiaohu 20120726 add for CR00658035 start
        if (gnFLYflag) {
            dialerKeyMap.put((char)russian_array[0][0], '2');
            dialerKeyMap.put((char)russian_array[0][1], '2');
            dialerKeyMap.put((char)russian_array[1][0], '2');
            dialerKeyMap.put((char)russian_array[1][1], '2');
            dialerKeyMap.put((char)russian_array[2][0], '2');
            dialerKeyMap.put((char)russian_array[2][1], '2');
            dialerKeyMap.put((char)russian_array[3][0], '2');
            dialerKeyMap.put((char)russian_array[3][1], '2');

            dialerKeyMap.put((char)russian_array[4][0], '3');
            dialerKeyMap.put((char)russian_array[4][1], '3');
            dialerKeyMap.put((char)russian_array[5][0], '3');
            dialerKeyMap.put((char)russian_array[5][1], '3');
            dialerKeyMap.put((char)russian_array[6][0], '3');
            dialerKeyMap.put((char)russian_array[6][1], '3');
            dialerKeyMap.put((char)russian_array[7][0], '3');
            dialerKeyMap.put((char)russian_array[7][1], '3');

            dialerKeyMap.put((char)russian_array[8][0], '4');
            dialerKeyMap.put((char)russian_array[8][1], '4');
            dialerKeyMap.put((char)russian_array[9][0], '4');
            dialerKeyMap.put((char)russian_array[9][1], '4');
            dialerKeyMap.put((char)russian_array[10][0], '4');
            dialerKeyMap.put((char)russian_array[10][1], '4');
            dialerKeyMap.put((char)russian_array[11][0], '4');
            dialerKeyMap.put((char)russian_array[11][1], '4');

            dialerKeyMap.put((char)russian_array[12][0], '5');
            dialerKeyMap.put((char)russian_array[12][1], '5');
            dialerKeyMap.put((char)russian_array[13][0], '5');
            dialerKeyMap.put((char)russian_array[13][1], '5');
            dialerKeyMap.put((char)russian_array[14][0], '5');
            dialerKeyMap.put((char)russian_array[14][1], '5');
            dialerKeyMap.put((char)russian_array[15][0], '5');
            dialerKeyMap.put((char)russian_array[15][1], '5');

            dialerKeyMap.put((char)russian_array[16][0], '6');
            dialerKeyMap.put((char)russian_array[16][1], '6');
            dialerKeyMap.put((char)russian_array[17][0], '6');
            dialerKeyMap.put((char)russian_array[17][1], '6');
            dialerKeyMap.put((char)russian_array[18][0], '6');
            dialerKeyMap.put((char)russian_array[18][1], '6');
            dialerKeyMap.put((char)russian_array[19][0], '6');
            dialerKeyMap.put((char)russian_array[19][1], '6');

            dialerKeyMap.put((char)russian_array[20][0], '7');
            dialerKeyMap.put((char)russian_array[20][1], '7');
            dialerKeyMap.put((char)russian_array[21][0], '7');
            dialerKeyMap.put((char)russian_array[21][1], '7');
            dialerKeyMap.put((char)russian_array[22][0], '7');
            dialerKeyMap.put((char)russian_array[22][1], '7');
            dialerKeyMap.put((char)russian_array[23][0], '7');
            dialerKeyMap.put((char)russian_array[23][1], '7');

            dialerKeyMap.put((char)russian_array[24][0], '8');
            dialerKeyMap.put((char)russian_array[24][1], '8');
            dialerKeyMap.put((char)russian_array[25][0], '8');
            dialerKeyMap.put((char)russian_array[25][1], '8');
            dialerKeyMap.put((char)russian_array[26][0], '8');
            dialerKeyMap.put((char)russian_array[26][1], '8');
            dialerKeyMap.put((char)russian_array[27][0], '8');
            dialerKeyMap.put((char)russian_array[27][1], '8');

            dialerKeyMap.put((char)russian_array[28][0], '9');
            dialerKeyMap.put((char)russian_array[28][1], '9');
            dialerKeyMap.put((char)russian_array[29][0], '9');
            dialerKeyMap.put((char)russian_array[29][1], '9');
            dialerKeyMap.put((char)russian_array[30][0], '9');
            dialerKeyMap.put((char)russian_array[30][1], '9');
            dialerKeyMap.put((char)russian_array[31][0], '9');
            dialerKeyMap.put((char)russian_array[31][1], '9');
        }
        // Gionee lixiaohu 20120726 add for CR00658035 end
    }
    // Gionee liuyanbo 20120422 merge for CR00576798 end
    
    // Gionee liuyanbo 2012-08-14 add for CR00674313 start
    public String HanziToPinyinString(final String input){        
        if(input == null || TextUtils.isEmpty(input)){
            // return empty tokens
            return null;
        }        
        StringBuilder returnStr = new StringBuilder();
        StringBuilder subStrSet = new StringBuilder();
        ArrayList<Token> tokens = new ArrayList<Token>();
        ArrayList<String> shortSubStrOffset = new ArrayList<String>();
        final int inputLength = input.length();
        final StringBuilder subString = new StringBuilder();
        final StringBuilder subStrOffset = new StringBuilder();
        int tokenType = Token.LATIN;
        int caseTypePre = DialerSearchToken.FIRSTCASE;
        int caseTypeCurr = DialerSearchToken.UPPERCASE;
        int mPos = 0;
        
        // Go through the input, create a new token when
        // a. Token type changed
        // b. Get the Pinyin of current charater.
        // c. current character is space.
        // d. Token case changed from lower case to upper case, 
        // e. the first character is always a separated one
        // f character == '+' || character == '#' || character == '*' || character == ',' || character == ';'
        for (int i = 0; i < inputLength; i++) {
            final char character = input.charAt(i);
            if (character == '-' || character == ',' ){
                mPos++;
            } else if (character == ' ') {
                if (subString.length() > 0) {
                    addToken(subString, tokens, tokenType);
                    addOffsets(subStrOffset, shortSubStrOffset);
                }
                mPos++;
                caseTypePre = DialerSearchToken.FIRSTCASE;
            } else if (character < 256) {
                if (tokenType != Token.LATIN && subString.length() > 0) {
                    addToken(subString, tokens, tokenType);
                    addOffsets(subStrOffset, shortSubStrOffset);
                 }
                caseTypeCurr = (character>='A' && character<='Z')?DialerSearchToken.UPPERCASE:DialerSearchToken.LOWERCASE;
                if(caseTypePre == DialerSearchToken.LOWERCASE && caseTypeCurr == DialerSearchToken.UPPERCASE){
                    addToken(subString, tokens, tokenType);
                    addOffsets(subStrOffset, shortSubStrOffset);
                }
                caseTypePre = caseTypeCurr; 
                tokenType = Token.LATIN;
                Character c = Character.toUpperCase(character);                              
                if(c != null){
                    subString.append(c);
                    subStrOffset.append((char)mPos);
                }
                mPos++;
            } else if (character < FIRST_UNIHAN) {
               // Gionee lixiaohu 20120726 add for CR00658035 start
               if (gnFLYflag) { 
                   if (tokenType != Token.UNKNOWN && subString.length() > 0) {
                        addToken(subString, tokens, tokenType);
                        addOffsets(subStrOffset, shortSubStrOffset);
                        caseTypePre = DialerSearchToken.FIRSTCASE;
                    }
                    tokenType = Token.UNKNOWN;
                    Character c = dialerKeyMap.get(character);
                    if(c != null){
                        subString.append(c);
                        subStrOffset.append((char)mPos);
                    }
                }
                // Gionee lixiaohu 20120726 add for CR00658035 end      
                mPos++;
            } else {
                Token t = getToken(character, true);
                int tokenSize = t.target.length();
                //Current type is PINYIN
                if (t.type == Token.PINYIN) {
                    if (subString.length() > 0) {
                        addToken(subString, tokens, tokenType);
                        addOffsets(subStrOffset, shortSubStrOffset);
                    }
                    tokens.add(t);
                    for(int j=0; j < tokenSize;j++)
                        subStrOffset.append((char)mPos);
                    addOffsets(subStrOffset,shortSubStrOffset);
                    tokenType = Token.PINYIN;
                    caseTypePre = DialerSearchToken.FIRSTCASE;
                    mPos++;
                } else { 
                    // Gionee lixiaohu 20120726 add for CR00658035 start 
                    if (gnFLYflag) {    
                        if (tokenType != t.type && subString.length() > 0) {
                            addToken(subString, tokens, tokenType);
                            addOffsets(subStrOffset, shortSubStrOffset);
                            caseTypePre = DialerSearchToken.FIRSTCASE;
                        }else{
                            caseTypeCurr = (character>='A' && character<='Z')?DialerSearchToken.UPPERCASE:DialerSearchToken.LOWERCASE;
                            if(caseTypePre == DialerSearchToken.LOWERCASE && caseTypeCurr == DialerSearchToken.UPPERCASE){
                                addToken(subString, tokens, tokenType);
                                addOffsets(subStrOffset, shortSubStrOffset);
                            }
                            caseTypePre = caseTypeCurr; 
                        }
                        tokenType = t.type;
                        Character c = dialerKeyMap.get(character);
                        if(c != null){
                            subString.append(c);
                            subStrOffset.append((char)mPos);
                        }
                    }   
                    // Gionee lixiaohu 20120726 add for CR00658035 end      
                    mPos++;
                }
            }
            //IF the name string is too long, cut it off to meet the storage request of dialer search.
            if(mPos > 127)
                break;
        }
        if (subString.length() > 0) {
            addToken(subString, tokens, tokenType); 
        }
        
        String lowerCaseString;
        char firstChar;
        char[] transChars = null;
        for (Token mtoken : tokens) {
            if (mtoken != null && mtoken.target != null) {
                if (mtoken.type == Token.PINYIN) {
                    lowerCaseString = mtoken.target.toLowerCase();
                    firstChar = lowerCaseString.charAt(0);
                    if (firstChar >= 'a' && firstChar <= 'z') {
                        transChars = lowerCaseString.toCharArray();
                        transChars[0] += LOW_TO_BIG_GAP;
                        returnStr.append(new String(transChars)).append(",");
                    } else {
                        returnStr.append(mtoken.target).append(",");
                    }
                } else {
                    String targetStr = mtoken.target;
                    StringBuilder tempStr = new StringBuilder();
                    for (int i = 0; i < targetStr.length(); i++) {
                        tempStr.append(targetStr.charAt(i)).append(",");
                    } 
                    if (tempStr.length() > 0) {
                        tempStr.deleteCharAt(tempStr.length() - 1);
                    }
                    returnStr.append(tempStr).append(","); 
                }
            }
        }
        /*for (int i = 0; i < returnStr.length(); i++) {
            returnStr.replace(i, i + 1, "" + Character.toUpperCase(returnStr.charAt(i)));
        }*/

        return returnStr.length() == 0 ? returnStr.toString() : returnStr
                .deleteCharAt(returnStr.length() - 1).toString().replaceAll("0", "");
    }
    private static final int LOW_TO_BIG_GAP = 'Z' - 'z'; 
    // Gionee liuyanbo 2012-08-14 add for CR00674313 end
}


