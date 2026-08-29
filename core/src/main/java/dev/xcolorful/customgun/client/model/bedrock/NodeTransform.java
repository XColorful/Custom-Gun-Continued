package dev.xcolorful.customgun.client.model.bedrock;

import org.jetbrains.annotations.ApiStatus;

/**
 * 单个模型节点的动画变换快照，用于延迟渲染下在 flush 阶段恢复 submit 时的节点状态
 */
@ApiStatus.AvailableSince("26.2")
public record NodeTransform(BedrockPart part,
                            float offsetX, float offsetY, float offsetZ,
                            float qx, float qy, float qz, float qw,
                            float scaleX, float scaleY, float scaleZ,
                            boolean visible) {

    public static NodeTransform capture(BedrockPart part) {
        return new NodeTransform(part,
                part.offsetX, part.offsetY, part.offsetZ,
                part.additionalQuaternion.x, part.additionalQuaternion.y, part.additionalQuaternion.z, part.additionalQuaternion.w,
                part.xScale, part.yScale, part.zScale,
                part.visible);
    }

    public void apply() {
        {
            part.offsetX = offsetX;
            part.offsetY = offsetY;
            part.offsetZ = offsetZ;
        }
        {
            part.additionalQuaternion.set(qx, qy, qz, qw);
        }
        {
            part.xScale = scaleX;
            part.yScale = scaleY;
            part.zScale = scaleZ;
        }
        {
            part.visible = visible;
        }
    }
}
