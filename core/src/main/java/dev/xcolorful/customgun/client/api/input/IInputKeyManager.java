/*
 * Go to BattleRoyale GameManager 的设计
 */

package dev.xcolorful.customgun.client.api.input;

import dev.xcolorful.customgun.client.input.InputKey;
import org.jetbrains.annotations.Nullable;

/**
 * 设计用途:
 * <ul>
 *     <li>集中监听键鼠事件并过滤</li>
 *     <li>避免{@link InputKey}需要同时检查键鼠输入</li>
 *     <li>不限制子类额外监听事件</li>
 * </ul>
 */
public interface IInputKeyManager extends IInputKeyMainManager {

    @Override
    default @Nullable IKeyMapping getKeyMapping() {
        return null;
    }
}
