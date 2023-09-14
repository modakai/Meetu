package com.sakura.meetu.service.impl;

import com.sakura.meetu.entity.Address;
import com.sakura.meetu.mapper.AddressMapper;
import com.sakura.meetu.service.IAddressService;
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
public class AddressServiceImpl extends ServiceImpl<AddressMapper, Address> implements IAddressService {

}
