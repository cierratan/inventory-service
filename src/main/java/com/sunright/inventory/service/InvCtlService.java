package com.sunright.inventory.service;

import com.sunright.inventory.dto.InvCtlDTO;
import com.sunright.inventory.dto.SearchRequest;
import com.sunright.inventory.dto.SearchResult;

public interface InvCtlService {
    InvCtlDTO createInvCtl(InvCtlDTO input);
    InvCtlDTO editInvCtl(InvCtlDTO input);
    InvCtlDTO findBy();
    void deleteInvCtl();
    SearchResult<InvCtlDTO> searchBy(SearchRequest searchRequest);
}
