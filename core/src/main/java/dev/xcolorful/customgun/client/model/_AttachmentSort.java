package dev.xcolorful.customgun.client.model;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class _AttachmentSort {

    /**
     * 是否忽略节点名称后缀匹配，直接按排序后的顺序绑定ocular和division
     * <ul>
     *     <li>开启后可以兼容ocular和division名称不完全对应的模型节点（对应原模组），但要求两者排序后的顺序一致</li>
     *     <li>关闭后仅相同名称后缀的节点会共享同一个stencil索引</li>
     * </ul>
     */
    @ApiStatus.Internal
    public static boolean IGNORE_NAME_MISMATCH = true;

    /**
     * 是否允许节点使用非数字后缀
     * <ul>
     *     <li>默认关闭，兼容原模组限制</li>
     *     <li>开启后则允许旧资源包使用非数字后缀的 ocular / division 节点名</li>
     * </ul>
     */
    @ApiStatus.Internal
    public static boolean ALLOW_NON_NUMERIC_SUFFIX = false;

    /**
     * 判断节点名去掉类型名（{@code ocular} / {@code division}）后剩下的后缀是否是「非数字后缀」
     * <ul>
     *     <li>空后缀（{@code ocular}、{@code division}）和纯数字后缀（{@code ocular_1}、{@code division_2}）都合法</li>
     *     <li>结果受{@link #ALLOW_NON_NUMERIC_SUFFIX}影响</li>
     *     <li>{@code division_illuminated}、{@code division_bg} 这类名字是 division 组内的普通节点，不能当作准心</li>
     * </ul>
     */
    public static boolean isStrippedSuffixInvalid(@Nullable String stripped) {
        if (ALLOW_NON_NUMERIC_SUFFIX) return false;
        if (stripped == null || stripped.isEmpty()) return false;
        for (int i = 0; i < stripped.length(); i++) {
            char c = stripped.charAt(i);
            if (c < '0' || c > '9') return true;
        }
        return false;
    }

    /**
     * 根据ocular和division的name，合并成一个list
     * <ul>
     *     <li>按节点名末尾的编号自然排序</li>
     *     <li>没有编号的（{@code ocular}、{@code ocular_sight}、{@code ocular_scope}、{@code division}）编号视作1，与原模组一致</li>
     *     <li>
     *         编号决定的是stencil索引，不能按字符串比较：{@code ocular_sight}编号为1、{@code ocular_scope_2}编号为2
     *         但{@code "_scope_2"}在字符串上小于{@code "_sight"}，直接比较会让这两者与division的配对反过来
     *     </li>
     *     <li>排序结果只是用来保证相同name节点使用相同的stencil，但是一般来说美术自己会让模型节点命名对上</li>
     *     <li>默认美术和BlockBench导出的json不会用同名节点，因此省略Set去重</li>
     * </ul>
     * 输入方处理{@link AttachmentModelObject#resetCache()}+排序{@link _AttachmentSort#getOcularDivisionSorted}示例:
     * <ol>
     *     ocular:
     *     <li>"ocular" -> ""</li>
     *     <li>"ocular_1" -> "1</li>
     *     <li>"ocular_2" -> "2"</li>
     *     <li>"ocular_10" -> "10"</li>
     *     <li>"ocular_scope" -> "_scope"</li>
     *     <li>"ocular_scope_1" -> "_scope_1"</li>
     *     <li>"ocular_sight" -> "_sight"</li>
     *     <li>"ocular_sight_1" -> "_sight_1"</li>
     * </ol>
     * <ol>
     *     division:
     *     <li>"division" -> ""</li>
     *     <li>"division_1" -> "1"</li>
     *     <li>"division_2" -> "2"</li>
     *     <li>"division_10" -> "10"</li>
     * </ol>
     * @return 合并ocular和division后的list
     */
    protected static ArrayList<AttachmentModelObject._Division_Ocular_Entry> getOcularDivisionSorted(List<AttachmentModelObject._OcularNodeEntry> ocularNodePaths,
                                                                                                     List<AttachmentModelObject._DivisionNodeEntry> divisionNodePaths) {
        ArrayList<AttachmentModelObject._Division_Ocular_Entry> result = new ArrayList<>();

        // ocular自身排序
        ocularNodePaths.sort(Comparator.comparing(AttachmentModelObject._OcularNodeEntry::name, _AttachmentSort::compareNodeIndex));
        // division自身排序
        divisionNodePaths.sort(Comparator.comparing(AttachmentModelObject._DivisionNodeEntry::name, _AttachmentSort::compareNodeIndex));

        // 两个列表双指针遍历
        int ocularIndex = 0;
        int divisionIndex = 0;
        while (ocularIndex < ocularNodePaths.size() || divisionIndex < divisionNodePaths.size()) {
            { // 其中一个指针已经到终点，另一个直接添加
                if (ocularIndex >= ocularNodePaths.size()) {
                    AttachmentModelObject._DivisionNodeEntry division = divisionNodePaths.get(divisionIndex++);
                    result.add(new AttachmentModelObject._Division_Ocular_Entry(division.name(), division.path()));
                    continue;
                } else if (divisionIndex >= divisionNodePaths.size()) {
                    AttachmentModelObject._OcularNodeEntry ocular = ocularNodePaths.get(ocularIndex++);
                    result.add(new AttachmentModelObject._Division_Ocular_Entry(ocular.name(), ocular));
                    continue;
                }
            }

            AttachmentModelObject._OcularNodeEntry ocular = ocularNodePaths.get(ocularIndex);
            AttachmentModelObject._DivisionNodeEntry division = divisionNodePaths.get(divisionIndex);

            if (IGNORE_NAME_MISMATCH) {
                result.add(new AttachmentModelObject._Division_Ocular_Entry(ocular.name(), ocular, division.path()));
                ocularIndex++;
                divisionIndex++;
                continue;
            }

            // 若两个字符串不同，则将更小的项添加进result，否则添加combined进result
            int compare = compareNodeSuffix(ocular.name(), division.name());
            if (compare < 0) {
                result.add(new AttachmentModelObject._Division_Ocular_Entry(ocular.name(), ocular));
                ocularIndex++;
            } else if (compare > 0) {
                result.add(new AttachmentModelObject._Division_Ocular_Entry(division.name(), division.path()));
                divisionIndex++;
            } else {
                result.add(new AttachmentModelObject._Division_Ocular_Entry(ocular.name(), ocular, division.path()));
                ocularIndex++;
                divisionIndex++;
            }
        }

        return result;
    }

    /**
     * 排序用比较器：按节点名末尾的编号比较
     * <ul>
     *     <li>末尾是数字（{@code "2"}、{@code "_sight_2"}）则取该数字</li>
     *     <li>末尾没有数字（{@code ""}、{@code "_sight"}、{@code "_scope"}）则视作1，与原模组给{@code ocular}/{@code ocular_sight}/{@code ocular_scope}的默认编号一致</li>
     * </ul>
     */
    private static int compareNodeIndex(String a, String b) {
        int compare = Integer.compare(getIndex(a), getIndex(b));
        if (compare != 0) return compare;
        return a.compareTo(b); // 编号相同时保持排序稳定
    }

    private static int getIndex(String name) {
        int begin = name.lastIndexOf('_') + 1;
        if (begin >= name.length()) return 1; // 空名或末尾是下划线

        int number = 0;
        for (int i = begin; i < name.length(); i++) {
            char c = name.charAt(i);
            if (c < '0' || c > '9') return 1; // 非数字后缀
            number = number * 10 + c - '0';
        }

        return number;
    }

    /**
     * 仅用于{@link #IGNORE_NAME_MISMATCH}关闭时按同名合并两个列表，不参与排序
     */
    private static int compareNodeSuffix(String a, String b) {
        int aNumber = getNumber(a);
        int bNumber = getNumber(b);

        if (aNumber >= 0 && bNumber >= 0) {
            return Integer.compare(aNumber, bNumber);
        }

        if (aNumber >= 0) {
            return -1;
        }

        if (bNumber >= 0) {
            return 1;
        }

        return a.compareTo(b);
    }

    private static int getNumber(String value) {
        if (value.length() < 2 || value.charAt(0) != '_') {
            return -1;
        }

        int number = 0;
        for (int i = 1; i < value.length(); i++) {
            char c = value.charAt(i);
            if (c < '0' || c > '9') {
                return -1;
            }
            number = number * 10 + c - '0';
        }

        return number;
    }
}
