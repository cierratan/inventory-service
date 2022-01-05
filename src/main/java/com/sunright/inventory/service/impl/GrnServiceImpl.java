package com.sunright.inventory.service.impl;

import com.sunright.inventory.dto.GrnDTO;
import com.sunright.inventory.entity.grn.Grn;
import com.sunright.inventory.entity.grn.GrnDet;
import com.sunright.inventory.entity.grn.GrnId;
import com.sunright.inventory.repository.GrnDetRepository;
import com.sunright.inventory.repository.GrnRepository;
import com.sunright.inventory.service.GrnService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Transactional
@Service
@Slf4j
public class GrnServiceImpl implements GrnService {

    @Autowired
    private GrnRepository grnRepository;

    @Autowired
    private GrnDetRepository grnDetRepository;

    @Override
    public GrnDTO createGrn(GrnDTO grnDTO) {

        try {
            GrnId grnId = new GrnId();
            BeanUtils.copyProperties(grnDTO, grnId);
            Grn grn = new Grn();
            BeanUtils.copyProperties(grnDTO, grn);
            SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
            String date = sdf.format(new Date());
            grn.setGrnId(grnId);
            grn.setRecdDate(sdf.parse(date));
            grn.setEntryDate(sdf.parse(date));
            Optional<Grn> grnOptional = grnRepository.findByGrnNo(grnDTO.getGrnNo());
            if (grnOptional.isPresent()) {
                throw new Exception("Duplicate record is detected. Please retry.");
            } else {
                log.info("Saving new GRN : {}", grn);
                grnRepository.save(grn);
                if (grnDTO.getGrnDetList() != null) {
                    List<GrnDet> grnDetList = new ArrayList<>();
                    for (int i = 0; i < grnDTO.getGrnDetList().size(); i++) {
                        GrnDet grnDet = new GrnDet();
                        BeanUtils.copyProperties(grnDTO, grnDet);
                        grnDet.setLoc(grnDTO.getGrnDetList().get(i).getLoc());
                        grnDet.setItemNo(grnDTO.getGrnDetList().get(i).getItemNo());
                        grnDet.setPartNo(grnDTO.getGrnDetList().get(i).getPartNo());
                        grnDet.setItemType(grnDTO.getGrnDetList().get(i).getItemType());
                        grnDet.setProjectNo(grnDTO.getGrnDetList().get(i).getProjectNo());
                        grnDet.setPoRecSeq(grnDTO.getGrnDetList().get(i).getPoRecSeq());
                        grnDet.setUom(grnDTO.getGrnDetList().get(i).getUom());
                        grnDet.setRecdQty(grnDTO.getGrnDetList().get(i).getRecdQty());
                        grnDet.setRecdPrice(grnDTO.getGrnDetList().get(i).getRecdPrice());
                        grnDet.setPoPrice(grnDTO.getGrnDetList().get(i).getPoPrice());
                        grnDet.setIssuedQty(grnDTO.getGrnDetList().get(i).getIssuedQty());
                        grnDet.setLabelQty(grnDTO.getGrnDetList().get(i).getLabelQty());
                        grnDet.setStdPackQty(grnDTO.getGrnDetList().get(i).getStdPackQty());
                        grnDet.setRemarks(grnDTO.getGrnDetList().get(i).getRemarks());
                        grnDet.setGrnId(grnId);
                        grnDet.setGrn(grn);
                        grnDet.setSeqNo(i + 1);
                        grnDet.setRecdDate(sdf.parse(date));
                        grnDetList.add(grnDet);
                    }
                    grnDetRepository.saveAll(grnDetList);
                    log.info("{} GRN Detail records saved. {}", grnDetList.size(), grnDetList);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return grnDTO;
    }

    @Override
    public List<Grn> list(int limit) {
        log.info("Fetching all GRN");
        return grnRepository.findAll(PageRequest.of(0, limit)).toList();
    }

    @Override
    public Boolean delete(GrnDTO input) {
        Grn grn = new Grn();
        GrnId grnId = new GrnId();
        BeanUtils.copyProperties(input, grn);
        BeanUtils.copyProperties(input, grnId);
        Optional<Grn> grnNoDel = grnRepository.findByGrnNo(input.getGrnNo());
        Optional<GrnDet> grnNoDelDet = grnDetRepository.findGrnDetByGrnNo(input.getGrnNo());
        if (!grnNoDel.isPresent() && !grnNoDelDet.isPresent()) {
            try {
                throw new Exception("Grn with grn no : " + input.getGrnNo() + " not found");
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            grnDetRepository.deleteById(grnId);
            grnRepository.deleteById(grnId);
            log.info("Deleting GRN by Grn No : {}", input.getGrnNo());
        }

        return true;
    }

    @Override
    public GrnDTO update(GrnDTO grnDTO) {

        try {
            GrnId grnId = new GrnId();
            BeanUtils.copyProperties(grnDTO, grnId);
            Grn grn = new Grn();
            BeanUtils.copyProperties(grnDTO, grn);
            SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
            String date = sdf.format(new Date());
            grn.setGrnId(grnId);
            grn.setRecdDate(sdf.parse(date));
            grn.setEntryDate(sdf.parse(date));
            log.info("Update GRN : {}", grn);
            grnRepository.save(grn);
            if (grnDTO.getGrnDetList() != null) {
                List<GrnDet> grnDetList = new ArrayList<>();
                for (int i = 0; i < grnDTO.getGrnDetList().size(); i++) {
                    GrnDet grnDet = new GrnDet();
                    BeanUtils.copyProperties(grnDTO, grnDet);
                    grnDet.setLoc(grnDTO.getGrnDetList().get(i).getLoc());
                    grnDet.setItemNo(grnDTO.getGrnDetList().get(i).getItemNo());
                    grnDet.setPartNo(grnDTO.getGrnDetList().get(i).getPartNo());
                    grnDet.setItemType(grnDTO.getGrnDetList().get(i).getItemType());
                    grnDet.setProjectNo(grnDTO.getGrnDetList().get(i).getProjectNo());
                    grnDet.setPoRecSeq(grnDTO.getGrnDetList().get(i).getPoRecSeq());
                    grnDet.setUom(grnDTO.getGrnDetList().get(i).getUom());
                    grnDet.setRecdQty(grnDTO.getGrnDetList().get(i).getRecdQty());
                    grnDet.setRecdPrice(grnDTO.getGrnDetList().get(i).getRecdPrice());
                    grnDet.setPoPrice(grnDTO.getGrnDetList().get(i).getPoPrice());
                    grnDet.setIssuedQty(grnDTO.getGrnDetList().get(i).getIssuedQty());
                    grnDet.setLabelQty(grnDTO.getGrnDetList().get(i).getLabelQty());
                    grnDet.setStdPackQty(grnDTO.getGrnDetList().get(i).getStdPackQty());
                    grnDet.setRemarks(grnDTO.getGrnDetList().get(i).getRemarks());
                    grnDet.setGrnId(grnId);
                    grnDet.setGrn(grn);
                    grnDet.setSeqNo(i + 1);
                    grnDet.setRecdDate(sdf.parse(date));
                    grnDetList.add(grnDet);
                }
                grnDetRepository.saveAll(grnDetList);
                log.info("{} GRN Detail updated. {}", grnDetList.size(), grnDetList);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return grnDTO;
    }
}
