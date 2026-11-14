package dev.xcolorful.customgun.client.model.bake;

import dev.xcolorful.customgun.client.model.bedrock.BedrockPart;
import org.jetbrains.annotations.ApiStatus;

import java.util.ArrayList;
import java.util.List;

/**
 * 一次状态段内的脏节点收集器（渲染重构B1-b）
 * <ul>
 *     <li>状态段遍历时逐节点比较 {@link BedrockPart#stateHash()}：变化即登记，供调用方决定在哪一层切开烘焙块</li>
 *     <li>同时收集「functional 回调直接发射几何」的节点：这类节点无法烘焙，只能整棵子树即时绘制</li>
 *     <li>节点状态与登记标记都存在节点上，因此同一个模型一帧内多次提交（光影阴影 pass）不会重复登记</li>
 * </ul>
 */
@ApiStatus.Internal
public final class BakeDirtyTracker {

    private long stamp = Long.MIN_VALUE;

    private final List<BedrockPart> dirty = new ArrayList<>();
    private final List<BedrockPart> immediate = new ArrayList<>();

    public void begin() {
        this.stamp++;
        this.dirty.clear();
        this.immediate.clear();
    }

    /**
     * 第一次看到某节点、或它的状态与上次不同时登记
     */
    public void check(BedrockPart part, long hash) {
        if (part.cgc$stateHash == hash) return;
        part.cgc$stateHash = hash;
        if (part.cgc$trackStamp == this.stamp) return;
        part.cgc$trackStamp = this.stamp;
        this.dirty.add(part);
    }

    /**
     * 本帧该节点直接发射了几何；每帧只登记一次
     * <li>调用方按帧处理，节点可能在第一帧就登记过（那一帧还没有分块），因此不能只依赖节点上的标记</li>
     */
    public void markImmediate(BedrockPart part) {
        if (part.cgc$immediateStamp == this.stamp) return;
        part.cgc$immediateStamp = this.stamp;
        this.immediate.add(part);
    }

    public List<BedrockPart> dirty() {
        return this.dirty;
    }

    public List<BedrockPart> immediate() {
        return this.immediate;
    }
}
