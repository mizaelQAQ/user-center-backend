package com.tfl.usercenter.common;

import lombok.Data;

import java.io.Serializable;
@Data


public class PageRequest implements Serializable {

    protected int pageSize = 10;


    protected int pageNum = 1;
}
