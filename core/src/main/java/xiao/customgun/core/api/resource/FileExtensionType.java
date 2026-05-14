package xiao.customgun.core.api.resource;

public enum FileExtensionType {
    // 标准格式
    JSON(FileExtensionName.JSON),
    OGG(FileExtensionName.SOUND_OGG),
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
