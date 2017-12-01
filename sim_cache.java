/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */


/**
 *
 * @author harika
 */

package sim_cache;

import java.io.*;
import java.util.*;
import java.lang.Math.*;


public class sim_cache {
    /**
     * @param args the command line arguments
     */
    
    
    public static void main(String[] args) {
        // TODO code application logic here
        int i;
        for(i = 0;i < args.length; i++){
        //System.out.println(args[i]);
        }
        int block_size = Integer.parseInt(args[0]);
        int size = Integer.parseInt(args[1]);
        int assoc = Integer.parseInt(args[2]);
        int victim_size = Integer.parseInt(args[3]);
        int victim_present;
        String victimPresent;
        if(victim_size > 0) {victim_present = 1; victimPresent = "y";}
        else {victim_present = 0; victimPresent = "n";}
        int block_size2 = block_size;
        int size2 = Integer.parseInt(args[4]);
        int assoc2 = Integer.parseInt(args[5]);
        float replacement_policy_trace = Float.parseFloat(args[6]);
        int replacement_policy;
        float lambda;
        
        //updating replacement policy
        if(replacement_policy_trace == 2) { replacement_policy = 0; lambda = 0;}
        else if(replacement_policy_trace == 3) { replacement_policy = 1; lambda = 0;}
        else { replacement_policy = 2;  lambda = replacement_policy_trace;}
        //System.out.println("lambda "+ lambda);
        //System.out.println("replacement"+replacement_policy);
        
        String fileName = args[7];
        long address_integer;
        
        CacheOperations L1 = new CacheOperations();
        L1.Cache_Create(block_size, size, assoc, victim_size,lambda);
        
        CacheOperations L2 = new CacheOperations();
        if(size2 > 0) L2.Cache_Create(block_size2, size2, assoc2, 0, 0);
        String line;
         try {
            // FileReader reads text files in the default encoding.
            FileReader fileReader = 
                new FileReader(fileName);

            // Always wrap FileReader in BufferedReader.
            BufferedReader bufferedReader = 
                new BufferedReader(fileReader);
            int temp =1;

            while((line = bufferedReader.readLine()) != null  ) {
                if(!line.isEmpty()){
                String[] linesplit = line.split(" ");
                address_integer = Long.parseLong(linesplit[1],16);
                //address_integer = address_integer >> 5;
                //System.out.println("read hex address " + linesplit[1]);
                
                if("r".equalsIgnoreCase(linesplit[0])){
                
                L1.Cache_Update(address_integer, replacement_policy,0,victim_present);
               
                if(size2 > 0){
                boolean L2_writeback_check = L1.Next_Cache_Instruction_WriteBack_Check();
                if(L2_writeback_check){
                long L2_writeback = L1.Next_Cache_Instruction_WriteBack();
                if(replacement_policy != 2) L2.Cache_Update(L2_writeback, replacement_policy,1,0);
                else L2.Cache_Update(L2_writeback, 0,1,0);
                }
                boolean L2_Forward = L1.Next_Cache_Instruction_Forward();
                if(L2_Forward) {
                    if(replacement_policy != 2) L2.Cache_Update(address_integer, replacement_policy,0,0);
                    else L2.Cache_Update(address_integer, 0,0,0);
                }
                
                }}
                else if("w".equalsIgnoreCase(linesplit[0])){
                
                 L1.Cache_Update(address_integer, replacement_policy,1,victim_present);
                
                 if(size2 > 0){
                boolean L2_writeback_check = L1.Next_Cache_Instruction_WriteBack_Check();
                if(L2_writeback_check){
                long L2_writeback = L1.Next_Cache_Instruction_WriteBack();
                if(replacement_policy != 2) L2.Cache_Update(L2_writeback, replacement_policy,1,0);
                else L2.Cache_Update(L2_writeback, 0,1,0);
                }
                boolean L2_Forward = L1.Next_Cache_Instruction_Forward();
                if(L2_Forward) {
                    if(replacement_policy != 2) L2.Cache_Update(address_integer, replacement_policy,0,0);
                    else L2.Cache_Update(address_integer, 0,0,0);
                }
                
                }}
               //remove=============================================================================================================
                //System.out.println("==================================="+temp+"=======================================");
                //L1.Cache_Print_CacheContents(block_size,size,assoc,replacement_policy,victimPresent,"L1");
                //L2.Cache_Print_CacheContents(block_size2,size,assoc2,replacement_policy,victimPresent,"L2");
                //remove=============================================================================================================
                 temp++;
                }   
                else
                    break;
            }
            // Always close files.
            bufferedReader.close(); 
        String rp = "0";
	if(replacement_policy == 0) rp = "LRU";
        else if(replacement_policy == 1) rp="LFU";
        else if(replacement_policy == 2) rp = "LRFU";
        System.out.println("  ===== Simulator configuration ===== ");
        System.out.println("  L1_BLOCKSIZE:                    " + block_size);
        System.out.println("  L1_SIZE:                       " + size);
        System.out.println("  L1_ASSOC:                         " + assoc);
        System.out.println("  Victim_Cache_SIZE:                    " + victim_size);
        System.out.println("  L2_SIZE:                       " + size2);
        System.out.println("  L2_ASSOC:                         " + assoc2);
        
        System.out.println("  trace_file:           " + fileName);
        System.out.println("  Replacement Policy:            " + rp);
        if(rp == "LRFU") {
            String lambda_format = String.format("%.2f", lambda);
            System.out.println("  lambda:           " + lambda_format);
        }
        System.out.println("  ===================================");
        System.out.println();
        
        
            L1.Cache_Print_CacheContents(block_size,size,assoc,replacement_policy,victimPresent,"L1");
            if(size2>0){
            L2.Cache_Print_CacheContents(block_size2,size,assoc2,replacement_policy,victimPresent,"L2");
            }
           
           System.out.println();
           System.out.println("====== Simulation results (raw) ======");
           System.out.println();
           L1.Cache_Print_RawMeasurements(block_size, size, assoc, "L1");
           L2.Cache_Print_RawMeasurements(block_size2, size2, assoc2, "L2");
           
           if(size2>0){
               double L1_MR = L1.Cache_Miss_Rate(block_size, size, assoc);
               double L1_MP = L1.Cache_Miss_Penalty(block_size, size, assoc);
               double L1_HT = L1.Cache_Hit_Time(block_size, size, assoc);
               double L2_MR = L2.Cache_Miss_Rate(block_size2, size2, assoc2);
               double L2_MP = L2.Cache_Miss_Penalty(block_size2, size2, assoc2);
               double L2_HT = (2.5+(2.5*(size2/(512.0*1024.0)))+(0.025*(block_size2/16.0))+(0.025*assoc2));
               long total_memory_traffic = L2.Cache_Memory_traffic(block_size2, size2, assoc2);
               float average_access_time = (float)(L1_HT+L1_MR*(L2_HT + (L2_MR*L2_MP))); 
               double daverage_access_time = (double) (Math.round(average_access_time * 10000.0)/10000.0);
               String dat = String.format("%.4f", daverage_access_time);
               System.out.println("  n. total memory traffic:         "+total_memory_traffic);
               System.out.println();
               System.out.println("  ==== Simulation results (performance) ====");
               System.out.println("  1. average access time:         "+dat+" ns");
           
           }
           else {L1.Cache_Print_Performance(block_size, size, assoc);}
        }
         catch(IOException ex) {
            System.out.println(
                "Error reading file '" 
                + fileName + "'");                  
        }
              
          
    }
    
    
    
}
