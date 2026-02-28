



package com.lms.batch.service;

import com.lms.batch.entity.Batch;
import com.lms.batch.entity.Branch;
import com.lms.batch.kafka.BatchLifecycleProducer;
import com.lms.batch.repository.BatchRepository;

import com.lms.batch.repository.BranchRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
public class BranchService {

    private final BranchRepository branchRepository;
    private final BatchRepository batchrepository;
    
    private final BatchLifecycleProducer batchlifecycleproducer;
   
    public BranchService(BranchRepository branchRepository,BatchRepository batchrepository,BatchLifecycleProducer batchlifecycleproducer) {
    	this.batchrepository=batchrepository;
        this.branchRepository = branchRepository;
       
        this.batchlifecycleproducer=batchlifecycleproducer;
       
    }

    /* ================= CREATE ================= */
    public Branch createBranch(Branch branch) {
        return branchRepository.save(branch);
    }

    /* ================= DELETE ================= */
    @Transactional
    public void deleteBranch(Long id) {

        Branch branch = branchRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Branch not found"));

        // 🔥 publish event -> BatchService will delete batches
        batchlifecycleproducer.branchDeleted(id);

        // delete branch
        branchRepository.delete(branch);

        System.out.println("🔥 BRANCH DELETED FROM DB -> " + id);
    }
    
    /* ================= READ ================= */
    public List<Branch> getAllBranches() {
        return branchRepository.findAll();
    }

    /* ================= UPDATE ================= */
    public Branch updateBranch(Long id, Branch updated) {

        Branch existing = branchRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Branch not found"));

        existing.setName(updated.getName());
        existing.setCity(updated.getCity());

        return branchRepository.save(existing);
    }

    public boolean existsById(Long id) {
        return branchRepository.existsById(id);
    }
   
}
