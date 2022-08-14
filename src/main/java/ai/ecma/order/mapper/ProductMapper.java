package ai.ecma.order.mapper;

import ai.ecma.lib.entity.Product;
import ai.ecma.lib.payload.resp.ProductRespDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

/**
 * @author Murtazayev Muhammad
 * @since 23.01.2022
 */
@Mapper(componentModel = "spring")
public interface ProductMapper {
    @Mappings({
            @Mapping(target = "categoryId", source = "category.id"),
            @Mapping(target = "photoId", source = "photo.id"),
            @Mapping(target = "discountId", source = "discount.id")
    })
    ProductRespDto toProductRespDto(Product product);
}