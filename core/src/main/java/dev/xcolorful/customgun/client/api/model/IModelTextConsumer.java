package dev.xcolorful.customgun.client.api.model;

import dev.xcolorful.customgun.client.resource.assets.display._ModelNodeTextDisplay;

import java.util.Map;

public interface IModelTextConsumer {

    void setTextShowList(Map<String, _ModelNodeTextDisplay> modelNodeTextDisplay);
}
