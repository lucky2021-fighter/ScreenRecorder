package com.serenegiant.gl;
/*
 * libcommon
 * utility/helper classes for myself
 *
 * Copyright (c) 2014-2023 saki t_saki@serenegiant.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
*/

import android.opengl.GLES11Ext;
import android.opengl.GLES20;
import android.opengl.GLES30;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import androidx.annotation.IntDef;

public interface GLConst {
	public static final int GL_TEXTURE_EXTERNAL_OES	= 0x8D65;
	public static final int GL_TEXTURE_2D           = 0x0DE1;
	/**
	 * 无效的纹理名称/纹理 ID
	 */
	public static final int GL_NO_TEXTURE = -1;
	/**
	 * 缓冲区名称/缓冲区 ID 无效
	 */
	public static final int GL_NO_BUFFER = -1;
	/**
	 * 着色器程序名称/着色器程序 ID 无效
	 */
	public static final int GL_NO_PROGRAM = -1;

// OpenGL|ES 关系中的所有常量都是整数，不能进行类型检查，因此您可能会犯错误并使用错误的参数。
// 既然可能是，所以你可以用注释来检查它，
// 纹理目标、纹理单元、插值方法和边缘缠绕/夹紧
// 定义常量的批注

	@IntDef({
		GL_TEXTURE_EXTERNAL_OES,
		GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
		GL_TEXTURE_2D,
		GLES20.GL_TEXTURE_2D,
		GLES30.GL_TEXTURE_2D,
	})
	@Retention(RetentionPolicy.SOURCE)
	public @interface TexTarget {}

	@IntDef({
		GLES20.GL_TEXTURE0,
		GLES20.GL_TEXTURE1,
		GLES20.GL_TEXTURE2,
		GLES20.GL_TEXTURE3,
		GLES20.GL_TEXTURE4,
		GLES20.GL_TEXTURE5,
		GLES20.GL_TEXTURE6,
		GLES20.GL_TEXTURE7,
		GLES20.GL_TEXTURE8,
		GLES20.GL_TEXTURE9,
		GLES20.GL_TEXTURE10,
		GLES20.GL_TEXTURE11,
		GLES20.GL_TEXTURE12,
		GLES20.GL_TEXTURE13,
		GLES20.GL_TEXTURE14,
		GLES20.GL_TEXTURE15,
		GLES20.GL_TEXTURE16,
		GLES20.GL_TEXTURE17,
		GLES20.GL_TEXTURE18,
		GLES20.GL_TEXTURE19,
		GLES20.GL_TEXTURE20,
		GLES20.GL_TEXTURE21,
		GLES20.GL_TEXTURE22,
		GLES20.GL_TEXTURE23,
		GLES20.GL_TEXTURE24,
		GLES20.GL_TEXTURE25,
		GLES20.GL_TEXTURE26,
		GLES20.GL_TEXTURE27,
		GLES20.GL_TEXTURE28,
		GLES20.GL_TEXTURE29,
		GLES20.GL_TEXTURE30,
		GLES20.GL_TEXTURE31,
	})
	@Retention(RetentionPolicy.SOURCE)
	public @interface TexUnit {}

	@IntDef({
		GLES20.GL_LINEAR,
		GLES20.GL_NEAREST,
		GLES30.GL_LINEAR,
		GLES30.GL_NEAREST,
	})
	@Retention(RetentionPolicy.SOURCE)
	public @interface MinMagFilter {}

	@IntDef({
		GLES20.GL_REPEAT,
		GLES20.GL_MIRRORED_REPEAT,
		GLES20.GL_CLAMP_TO_EDGE,
		GLES30.GL_REPEAT,
		GLES30.GL_MIRRORED_REPEAT,
		GLES30.GL_CLAMP_TO_EDGE,
	})
	@Retention(RetentionPolicy.SOURCE)
	public @interface Wrap {}
}
