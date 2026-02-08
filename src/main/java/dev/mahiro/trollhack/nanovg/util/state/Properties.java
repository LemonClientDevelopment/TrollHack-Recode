package dev.mahiro.trollhack.nanovg.util.state;

public final class Properties {
    private final int[] lastUnpackSkipImages = new int[1];
    private final int[] lastUnpackImageHeight = new int[1];
    private final int[] lastPackSkipImages = new int[1];
    private final int[] lastPackImageHeight = new int[1];
    private final int[] lastUnpackSkipRows = new int[1];
    private final int[] lastUnpackSkipPixels = new int[1];
    private final int[] lastUnpackRowLength = new int[1];
    private final int[] lastUnpackAlignment = new int[1];
    private final int[] lastUnpackLsbFirst = new int[1];
    private final int[] lastUnpackSwapBytes = new int[1];
    private final int[] lastPackAlignment = new int[1];
    private final int[] lastPackSkipRows = new int[1];
    private final int[] lastPackSkipPixels = new int[1];
    private final int[] lastPackRowLength = new int[1];
    private final int[] lastPackLsbFirst = new int[1];
    private final int[] lastPackSwapBytes = new int[1];

    private final int[] lastActiveTexture = new int[1];
    private final int[] lastProgram = new int[1];
    private final int[] lastTexture = new int[1];
    private final int[] lastSampler = new int[1];
    private final int[] lastArrayBuffer = new int[1];
    private final int[] lastVertexArrayObject = new int[1];
    private final int[] lastPolygonMode = new int[2];
    private final int[] lastViewport = new int[4];
    private final int[] lastScissorBox = new int[4];
    private final int[] lastBlendSrcRgb = new int[1];
    private final int[] lastBlendDstRgb = new int[1];
    private final int[] lastBlendSrcAlpha = new int[1];
    private final int[] lastBlendDstAlpha = new int[1];
    private final int[] lastBlendEquationRgb = new int[1];
    private final int[] lastBlendEquationAlpha = new int[1];
    private final int[] lastPixelUnpackBufferBinding = new int[1];

    private boolean lastEnableBlend;
    private boolean lastEnableCullFace;
    private boolean lastEnableDepthTest;
    private boolean lastEnableStencilTest;
    private boolean lastEnableScissorTest;
    private boolean lastEnablePrimitiveRestart;
    private boolean lastDepthMask;

    public int[] getLastUnpackSkipImages() {
        return lastUnpackSkipImages;
    }

    public int[] getLastUnpackImageHeight() {
        return lastUnpackImageHeight;
    }

    public int[] getLastPackSkipImages() {
        return lastPackSkipImages;
    }

    public int[] getLastPackImageHeight() {
        return lastPackImageHeight;
    }

    public int[] getLastUnpackSkipRows() {
        return lastUnpackSkipRows;
    }

    public int[] getLastUnpackSkipPixels() {
        return lastUnpackSkipPixels;
    }

    public int[] getLastUnpackRowLength() {
        return lastUnpackRowLength;
    }

    public int[] getLastUnpackAlignment() {
        return lastUnpackAlignment;
    }

    public int[] getLastUnpackLsbFirst() {
        return lastUnpackLsbFirst;
    }

    public int[] getLastUnpackSwapBytes() {
        return lastUnpackSwapBytes;
    }

    public int[] getLastPackAlignment() {
        return lastPackAlignment;
    }

    public int[] getLastPackSkipRows() {
        return lastPackSkipRows;
    }

    public int[] getLastPackSkipPixels() {
        return lastPackSkipPixels;
    }

    public int[] getLastPackRowLength() {
        return lastPackRowLength;
    }

    public int[] getLastPackLsbFirst() {
        return lastPackLsbFirst;
    }

    public int[] getLastPackSwapBytes() {
        return lastPackSwapBytes;
    }

    public int[] getLastActiveTexture() {
        return lastActiveTexture;
    }

    public int[] getLastProgram() {
        return lastProgram;
    }

    public int[] getLastTexture() {
        return lastTexture;
    }

    public int[] getLastSampler() {
        return lastSampler;
    }

    public int[] getLastArrayBuffer() {
        return lastArrayBuffer;
    }

    public int[] getLastVertexArrayObject() {
        return lastVertexArrayObject;
    }

    public int[] getLastPolygonMode() {
        return lastPolygonMode;
    }

    public int[] getLastViewport() {
        return lastViewport;
    }

    public int[] getLastScissorBox() {
        return lastScissorBox;
    }

    public int[] getLastBlendSrcRgb() {
        return lastBlendSrcRgb;
    }

    public int[] getLastBlendDstRgb() {
        return lastBlendDstRgb;
    }

    public int[] getLastBlendSrcAlpha() {
        return lastBlendSrcAlpha;
    }

    public int[] getLastBlendDstAlpha() {
        return lastBlendDstAlpha;
    }

    public int[] getLastBlendEquationRgb() {
        return lastBlendEquationRgb;
    }

    public int[] getLastBlendEquationAlpha() {
        return lastBlendEquationAlpha;
    }

    public int[] getLastPixelUnpackBufferBinding() {
        return lastPixelUnpackBufferBinding;
    }

    public boolean isLastEnableBlend() {
        return lastEnableBlend;
    }

    public void setLastEnableBlend(boolean lastEnableBlend) {
        this.lastEnableBlend = lastEnableBlend;
    }

    public boolean isLastEnableCullFace() {
        return lastEnableCullFace;
    }

    public void setLastEnableCullFace(boolean lastEnableCullFace) {
        this.lastEnableCullFace = lastEnableCullFace;
    }

    public boolean isLastEnableDepthTest() {
        return lastEnableDepthTest;
    }

    public void setLastEnableDepthTest(boolean lastEnableDepthTest) {
        this.lastEnableDepthTest = lastEnableDepthTest;
    }

    public boolean isLastEnableStencilTest() {
        return lastEnableStencilTest;
    }

    public void setLastEnableStencilTest(boolean lastEnableStencilTest) {
        this.lastEnableStencilTest = lastEnableStencilTest;
    }

    public boolean isLastEnableScissorTest() {
        return lastEnableScissorTest;
    }

    public void setLastEnableScissorTest(boolean lastEnableScissorTest) {
        this.lastEnableScissorTest = lastEnableScissorTest;
    }

    public boolean isLastEnablePrimitiveRestart() {
        return lastEnablePrimitiveRestart;
    }

    public void setLastEnablePrimitiveRestart(boolean lastEnablePrimitiveRestart) {
        this.lastEnablePrimitiveRestart = lastEnablePrimitiveRestart;
    }

    public boolean isLastDepthMask() {
        return lastDepthMask;
    }

    public void setLastDepthMask(boolean lastDepthMask) {
        this.lastDepthMask = lastDepthMask;
    }
}

