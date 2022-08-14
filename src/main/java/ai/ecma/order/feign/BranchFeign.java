package ai.ecma.order.feign;

import ai.ecma.lib.payload.ApiResult;
import ai.ecma.lib.payload.resp.BranchRespDto;
import ai.ecma.order.utils.AppConstant;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * This interface not documented :(
 *
 * @author Muhammad Mo'minov
 * @since 31.01.2022
 */
@FeignClient(name = BranchFeign.BASE_PATH, configuration = FeignConfig.class)
public interface BranchFeign {
    String BASE_PATH = AppConstant.BRANCH_SERVICE + "/api/branch/v1";

    @GetMapping("/branch/nearly")
    ApiResult<BranchRespDto> getNearly(@RequestParam(name = "lat") Double lat,
                                       @RequestParam(name = "lon") Double lon);

}
