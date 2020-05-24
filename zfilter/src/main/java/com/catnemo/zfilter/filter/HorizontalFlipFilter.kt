package com.catnemo.zfilter.filter

import com.catnemo.zfilter.R

/**
 * @description
 * @author  franticzhou
 * @date    2019-12-17   13:40
 */
class HorizontalFlipFilter : BaseFilter {

    constructor() : super(readShaderFromRaw(R.raw.defualt_vertex_shader), readShaderFromRaw(R.raw.fragment_horizontal_flip))
}