package com.catnemo.zfilter.filter

import com.catnemo.zfilter.R

/**
 * @description 三屏特效
 * @author  franticzhou
 * @date    2019-12-13   19:32
 */
class NineScreenFilter : BaseFilter {
    constructor() : super(readShaderFromRaw(R.raw.defualt_vertex_shader), readShaderFromRaw(R.raw.fragment_nine_screen))
}