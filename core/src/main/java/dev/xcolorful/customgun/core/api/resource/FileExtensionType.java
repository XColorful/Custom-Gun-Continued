package dev.xcolorful.customgun.core.api.resource;

public enum FileExtensionType {
    // 标准格式
    JSON(FileExtensionName.JSON),
    PNG(FileExtensionName.PNG),
    OGG(FileExtensionName.SOUND_OGG),
    BEDROCK_ANIMATION(FileExtensionName.BEDROCK_ANIMATION),
    GLTF(FileExtensionName.GLTF),
    LUA(FileExtensionName.LUA),
    // 非标准格式
    GUNPACK_META(FileExtensionName.GUNPACK_META);

    final String extensionName;
    final String extensionNameWithDot;
    FileExtensionType(String fileExtension) {
        this.extensionName = fileExtension;
        this.extensionNameWithDot = "." + fileExtension;
    }

    public String getExtensionName() {
        return extensionName;
    }
    public String getExtensionNameWithDot() {
        return extensionNameWithDot;
    }
}
