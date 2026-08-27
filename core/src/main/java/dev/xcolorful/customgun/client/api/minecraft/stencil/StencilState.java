package dev.xcolorful.customgun.client.api.minecraft.stencil;

public class StencilState {
    
    public StencilFunction sFrontFunc = StencilFunction.ALWAYS;
    public StencilFunction sBackFunc = StencilFunction.ALWAYS;
    public int sRef = 0;
    public int sReadMask = 0xFF;
    public StencilOperation sFrontFail = StencilOperation.KEEP;
    public StencilOperation sFrontDepthFail = StencilOperation.KEEP;
    public StencilOperation sFrontPass = StencilOperation.KEEP;
    public StencilOperation sBackFail = StencilOperation.KEEP;
    public StencilOperation sBackDepthFail = StencilOperation.KEEP;
    public StencilOperation sBackPass = StencilOperation.KEEP;
    public int sWriteMask = 0xFF;

    public boolean sColorWriteOff = false;
    public boolean sDepthWriteOff = false;
    public boolean sDepthTestOff = false;
    
    public StencilState() {
    }
}
