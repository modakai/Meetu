package com.sakura.meetu.service.impl;

import com.sakura.meetu.entity.Orders;
import com.sakura.meetu.mapper.OrdersMapper;
import com.sakura.meetu.service.IOrdersService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author sakura
 * @since 2023-09-14
 */
@Service
public class OrdersServiceImpl extends ServiceImpl<OrdersMapper, Orders> implements IOrdersService {

}
