package com.atguigu.gmall.model.base;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

@Data
public class BaseEntity implements Serializable {

    @ApiModelProperty(value = "id")
    @TableId(type = IdType.AUTO)
    private Long id;

//    @ApiModelProperty(value = "创建时间")
//    @TableField(fill = FieldFill.INSERT)
//    private Date gmtCreate;
//
//    @ApiModelProperty(value = "更新时间")
//    @TableField(fill = FieldFill.INSERT_UPDATE)
//    private Date gmtModified;
//
//    @ApiModelProperty(value = "逻辑删除 1（true）已删除， 0（false）未删除")
//    @TableLogic
//    @TableField(value = "is_deleted")
//    private Boolean deleted;
}
