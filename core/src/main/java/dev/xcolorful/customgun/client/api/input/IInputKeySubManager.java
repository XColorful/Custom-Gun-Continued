/*
 * Go to BattleRoyale GameManager 的设计
 */

package dev.xcolorful.customgun.client.api.input;

/**
 * 因为设计成: 一个 {@link IInputKeySubManager} 对应一个按键
 * <br>
 * 所以: {@link IInputKeyManager}按键过滤能覆盖单个{@link IInputKeySubManager}
 */
public interface IInputKeySubManager extends IInputHandler {

    String getManagerName();

    boolean registerEventHandler();
    boolean unregisterEventHandler();

    IKeyMapping getKeyMapping();
}
