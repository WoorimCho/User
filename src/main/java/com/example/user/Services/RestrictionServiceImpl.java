package com.example.user.Services;

import com.example.user.Dto.RestrictionResponse;
import com.example.user.Exception.NotFoundException;
import com.example.user.Repositories.RestrictionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class RestrictionServiceImpl implements RestrictionService {

    private final RestrictionRepository restrictions;

    public RestrictionServiceImpl(RestrictionRepository restrictions) {
        this.restrictions = restrictions;
    }

    @Override
    public List<RestrictionResponse> list(String kind) {
        List<com.example.user.Model.Restriction> found = StringUtils.hasText(kind)
                ? restrictions.findByKindIgnoreCaseOrderByLabelAsc(kind.trim())
                : restrictions.findAllByOrderByKindAscLabelAsc();
        return found.stream().map(RestrictionResponse::from).toList();
    }

    @Override
    public RestrictionResponse getByCode(String code) {
        return restrictions.findByCodeIgnoreCase(code.trim())
                .map(RestrictionResponse::from)
                .orElseThrow(() -> NotFoundException.of("Restriction", code));
    }
}
