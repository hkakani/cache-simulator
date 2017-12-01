/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package sim_cache;

import java.lang.*;
import static java.lang.Math.pow;
import java.util.*;

/**
 *
 * @author harika
 */
public class CacheOperations {

    /**
     *index is no of sets for given parameters
     * 
     */
    public int index;
    public int index_bits;
    public int block_bits;
    public long[][] cache_array;
    public long[][] cache_addresses;
    public long[] victim_cache;
    public long[] victim_valid_bits;
    public long[] victim_dirty_bits;
    public long[] victim_lru_counter;
    public long[] victim_addresses;
    
    public long[][] valid_bits;
    public long[][] lru_counter;
    public long[][] dirty_bits;
    public int no_of_tags;
    public long read_cache_miss;
    public long read_cache_hit;
    public long write_cache_miss;
    public long write_back;
    public long victim_write_back;
    public long no_of_reads;
    public long no_of_writes;
    public long count_block[][];
    public long[] count_set;
    public long total_memory_traffic;
    public double average_access_time;
    public double miss_rate;
    public double miss_penalty;
    public double cache_hit_time;
    public int swap;
    public int no_of_victim_blocks = 0;
    
    public boolean fwd;
    public long evict_address;
    public boolean writeback_bool;
    
    //LRFU variables
    public double Global_counter;
    public double Last_Time_Stamp[][];
    public double CRF[][];
    public float lambda_value;
    
    
    
    public void Cache_Create(int block_size,int size,int assoc,int victim_size,float lambda){
    no_of_tags = assoc;
    index = (size)/(block_size*assoc);
    index_bits = (int) (Math.log(index)/Math.log(2));
    block_bits = (int) (Math.log(block_size)/Math.log(2));
    read_cache_hit = 0;
    read_cache_miss = 0;
    write_cache_miss =0;
    write_back =0;
    no_of_reads = 0;
    no_of_writes = 0;
    total_memory_traffic =0;
    average_access_time = 0;
    miss_rate = 0;
    miss_penalty = 0;
    cache_hit_time = 0;
    
    if(lambda != 0) lambda_value = lambda;
    else lambda_value = 0;
    
    //System.out.println("index: "+ index_bits + " block: " +block_bits);    
    
    cache_array = new long[index][assoc];
    
    cache_addresses = new long[index][assoc];
    
    valid_bits = new long[index][assoc];
    for (long[] row : valid_bits)
        Arrays.fill(row, 0);
    
    lru_counter = new long[index][assoc];
    for (long[] row : lru_counter)
        Arrays.fill(row, 0);
    
    count_block = new long[index][assoc];
    for(long[] row : count_block)
        Arrays.fill(row,0);
    
    count_set = new long[index];
    Arrays.fill(count_set, 0);
    
    dirty_bits = new long[index][assoc];
    for (long[] row : dirty_bits)
        Arrays.fill(row, 0);
    
    for (int i=0;i<index;i++){
        int fill = no_of_tags - 1;
        for (int j=0;j<no_of_tags;j++){
            lru_counter[i][j] = fill;
            fill--;
        }
        
        Last_Time_Stamp = new double[index][assoc];
        for (double[] row : Last_Time_Stamp)
        Arrays.fill(row, 0);
    
    CRF = new double[index][assoc];
    for (double[] row : CRF)
        Arrays.fill(row, 0);
    }
    
    //if victim cache is present them
    if(victim_size > 0)
    Create_Victim_Cache(victim_size,block_size);
    
    }
    
    public void Create_Victim_Cache(int victim_size, int block_size){
    
        no_of_victim_blocks = victim_size/block_size;
        
        swap =0;
        victim_cache = new long[no_of_victim_blocks];
        victim_addresses = new long[no_of_victim_blocks];
        victim_valid_bits = new long[no_of_victim_blocks];
        Arrays.fill(victim_valid_bits,0);
         
        victim_dirty_bits = new long[no_of_victim_blocks];
        Arrays.fill(victim_dirty_bits,0);
        
        victim_lru_counter = new long[no_of_victim_blocks];
         
        //System.out.println(no_of_victim_blocks);
        int fill = no_of_victim_blocks - 1;
        for (int i=0;i<no_of_victim_blocks;i++){
            
            victim_lru_counter[i] = fill;
            fill--;
        }
    }
    
    public void updateCounters_Cache(int columnNum,int index_read,int replacement_policy, int hitOrmiss){
        
        //LRU policy and hit in L1
        if(replacement_policy == 0 && hitOrmiss == 1){
            long temp = lru_counter[index_read][columnNum]; 
            for(int j=0; j< no_of_tags; j++){
                if(j == columnNum) lru_counter[index_read][j] = 0;
                else {
                if(lru_counter[index_read][j] < temp) lru_counter[index_read][j] += 1;
                }
            }
        }
        
        
        //LFU policy and hit in L1
        if(replacement_policy == 1 && hitOrmiss == 1){
            count_block[index_read][columnNum] += 1;
        }
        
        //LRFU and hit in L1
         if(replacement_policy == 2 && hitOrmiss == 1){
             double power = pow(0.5,(lambda_value*(Global_counter-Last_Time_Stamp[index_read][columnNum])));
            CRF[index_read][columnNum] = 1+power*CRF[index_read][columnNum];
            Last_Time_Stamp[index_read][columnNum] = Global_counter;
        }
        
        
        //LRU and Miss in L1
        if(replacement_policy == 0 && hitOrmiss == 0){ 
             for(int j=0; j< no_of_tags; j++){
                if(j == columnNum) lru_counter[index_read][j] = 0;
                else lru_counter[index_read][j] += 1;
            }           
        
        }
        
        //LFU and Miss in L1
        if(replacement_policy == 1 && hitOrmiss == 0){
            count_set[index_read] = count_block[index_read][columnNum];
            count_block[index_read][columnNum] = count_set[index_read] + 1;
        
        }
        
        //LRFU miss
        if(replacement_policy == 2 && hitOrmiss == 0){
            CRF[index_read][columnNum] = 1;
            Last_Time_Stamp[index_read][columnNum] = Global_counter;
            
        }
        
    }
    
    public int find_evict_block(int index_read,int replacement_policy){
        int evict_columnNum = 0;
        int temp_column = -1;
        
        if(replacement_policy == 0){
            long max_count = lru_counter[index_read][0];
            for(long countIterate : lru_counter[index_read]){
                temp_column++;
                if(max_count < countIterate){
                    evict_columnNum = temp_column;
                    max_count = countIterate;
                }
            }
        }
        
        if(replacement_policy == 1){
            long min_count = count_block[index_read][0];
            for(long countIterate : count_block[index_read]){
                temp_column++;
                if(min_count > countIterate){
                    evict_columnNum = temp_column;
                    min_count = countIterate;
                }
            }
        }
            
        if(replacement_policy == 2){
            double temp_crf;
            double CRF_min;
            double power = pow(0.5,(lambda_value*(Global_counter-Last_Time_Stamp[index_read][0])));
                CRF_min = CRF[index_read][0]*power;
            for(double iterate : CRF[index_read]){
                temp_column++;
                double temp_power = pow(0.5,(lambda_value*(Global_counter-Last_Time_Stamp[index_read][temp_column])));
                temp_crf = iterate*temp_power;
                if(CRF_min>temp_crf){
                    evict_columnNum = temp_column;
                    CRF_min = temp_crf;
                }
            
            }
        
        }
            
          
        
        
        
    return evict_columnNum;
    }
       

    
    public void Cache_Performance(int blocksize,int size,int assoc){
        float block_size = blocksize;
        float cache_size = size;
        float cache_assoc = assoc;
        if(no_of_victim_blocks == 0)  total_memory_traffic = read_cache_miss + write_cache_miss + write_back;
        else  total_memory_traffic = read_cache_miss + write_cache_miss + victim_write_back;
        miss_rate = (read_cache_miss*1.0 + write_cache_miss*1.0)/(no_of_reads*1.0 + no_of_writes*1.0);
        miss_penalty =  (20.0 + ((0.5)*((block_size)/16.0)));
        cache_hit_time = (0.25+(2.5*(cache_size/(512.0*1024.0)))+(0.025*(block_size/16.0))+(0.025*cache_assoc));
        average_access_time = (cache_hit_time + (miss_rate*miss_penalty));    
    }
    
    public double Cache_Miss_Rate(int blocksize,int size,int assoc){
        Cache_Performance(blocksize,size,assoc);
        //float dmiss_rate = (float) (Math.round(miss_rate * 10000.0)/10000.0);
        //double daverage_access_time = (double) (Math.round(average_access_time * 10000.0)/10000.0); 
     return miss_rate;
    }
    
    public double Cache_Miss_Penalty(int blocksize,int size,int assoc){
        Cache_Performance(blocksize,size,assoc);
        //double dmiss_rate = (double) (Math.round(miss_rate * 10000.0)/10000.0);
        //double daverage_access_time = (double) (Math.round(average_access_time * 10000.0)/10000.0); 
     return miss_penalty;
    }
    
    public double Cache_Hit_Time(int blocksize,int size,int assoc){
        Cache_Performance(blocksize,size,assoc);
        //double dmiss_rate = (double) (Math.round(miss_rate * 10000.0)/10000.0);
        //double daverage_access_time = (double) (Math.round(average_access_time * 10000.0)/10000.0); 
     return cache_hit_time;
    }
    
    public long Cache_Memory_traffic(int blocksize,int size,int assoc){
        Cache_Performance(blocksize,size,assoc);
        //double dmiss_rate = (double) (Math.round(miss_rate * 10000.0)/10000.0);
        //double daverage_access_time = (double) (Math.round(average_access_time * 10000.0)/10000.0); 
     return total_memory_traffic;
    }
    
    
    public void Cache_Print_CacheContents(int blocksize,int size,int assoc,int replacement_policy,String victim_present,String L1orL2){
      
       if(L1orL2 == "L1"){
           System.out.println("===== L1 contents =====");
       //===================================================remove=======================================
            //System.out.println("===== GLobal Counter: "+ Global_counter+"=====");
       //===================================================remove=======================================

           for(int i=0;i<index;i++){
		   System.out.print("set \t" + i + ":\t");
		   for(int j=0;j<no_of_tags;j++){
			   if(dirty_bits[i][j] == 1) System.out.print( Long.toHexString(cache_array[i][j]) + " D\t");
			   else System.out.print( Long.toHexString(cache_array[i][j]) + " \t");
                           //===================================================remove=======================================
                           //System.out.print(CRF[i][j]+ "\t");
                           //System.out.print(Last_Time_Stamp[i][j]+ "\t");
                           //================================================remove=========================================
			}
		    System.out.println();
		}
       }
       
       
       else if(L1orL2 == "L2" ){
           System.out.println("===== L2 contents =====");
            for(int i=0;i<index;i++){
		   System.out.print("set \t" + i + ":\t");
		   for(int j=0;j<no_of_tags;j++){
			   if(dirty_bits[i][j] == 1) System.out.print( Long.toHexString(cache_array[i][j]) + " D\t");
			   else System.out.print( Long.toHexString(cache_array[i][j]) + " \t");
			}
		    System.out.println();
		}
       }
       
	  
		if(victim_present.contains("y") && L1orL2 == "L1" ){
			System.out.println("===== Victim Cache contents =====");
			System.out.print("set \t" + "0" + ":\t");
			for(int i=0;i<no_of_victim_blocks;i++){
                
                    if(victim_dirty_bits[i] == 1) System.out.print( Long.toHexString(victim_addresses[i]>>block_bits) + " D\t");
                    else System.out.print( Long.toHexString(victim_addresses[i]>>block_bits) + " \t");
            } 
		System.out.println();       
		}

       
       
    }
    
    public void Cache_Print_RawMeasurements(int blocksize,int size,int assoc,String L1orL2){
        if(L1orL2.contains("L1")){  
        Cache_Performance(blocksize,size,assoc);
        double dmiss_rate = (double) (Math.round(miss_rate * 10000.0)/10000.0);
        String dmr = String.format("%.4f", dmiss_rate);
        double daverage_access_time = (double) (Math.round(average_access_time * 10000.0)/10000.0); 
        System.out.println("  a. number of L1 reads:           " + no_of_reads);
        System.out.println("  b. number of L1 read misses:      " + read_cache_miss);
        System.out.println("  c. number of L1 writes:          " + no_of_writes);
        System.out.println("  d. number of L1 write misses:     " + write_cache_miss);
        System.out.println("  e. L1 miss rate:                " + dmr);
        System.out.println("  f. number of swaps:     "+swap);
        System.out.println("  g. number of victim cache writeback:     "+victim_write_back);
    }
        if(L1orL2.contains("L2")){
        Cache_Performance(blocksize,size,assoc);
        double dmiss_rate = (double) (Math.round(miss_rate * 10000.0)/10000.0);
        String dmr ="0";
        if(dmiss_rate == 0) dmr = "0";
        else if(dmiss_rate != 0) dmr= String.format("%.4f", dmiss_rate);
        double daverage_access_time = (double) (Math.round(average_access_time * 10000.0)/10000.0); 
        System.out.println("  h. number of L2 reads:           " + no_of_reads);
        System.out.println("  i. number of L2 read misses:      " + read_cache_miss);
        System.out.println("  j. number of L2 writes:          " + no_of_writes);
        System.out.println("  k. number of L2 write misses:     " + write_cache_miss);
        System.out.println("  l. L2 miss rate:                " + dmr);
        System.out.println("  m. number of L2 writeback:     "+write_back);
        }
        
    }
    
    public void Cache_Print_Performance(int blocksize,int size,int assoc){
        Cache_Performance(blocksize,size,assoc);
        double dmiss_rate = (double) (Math.round(miss_rate * 10000.0)/10000.0);
        double daverage_access_time = (double) (Math.round(average_access_time * 10000.0)/10000.0); 
        String dat = String.format("%.4f", daverage_access_time);
        System.out.println("  n. total memory traffic:         "+total_memory_traffic);
        System.out.println();
        System.out.println("  ==== Simulation results (performance) ====");
        System.out.println("  1. average access time:         "+dat+" ns");
    }
    
    
     public boolean Next_Cache_Instruction_Forward(){
        return fwd;
    }
    
    public long Next_Cache_Instruction_WriteBack(){
        return evict_address;
    }
    public boolean Next_Cache_Instruction_WriteBack_Check(){
    return writeback_bool;
    } 
    
    public void Cache_Update(long address,int replacement_policy, int readOrwrite, int victimPresent){
         fwd = false;
         evict_address = 0;
         writeback_bool = false;
         long shift_address = index_bits + block_bits;
         long tag;
         int index;
         long mask = ~((-1)<<shift_address);
         int temp =0,columnNum = 0,hitOrmiss_cache=0,evict_columnNum=0;
         
        //tag value from the address
        tag = address >> shift_address;
                
        //index value from address
        int temp_index;
        temp_index = (int)(address & mask);
        index = (temp_index >> block_bits);
        
        //System.out.println("address tag " + Long.toHexString(address) +" " + Long.toHexString(tag));
        if(readOrwrite == 0) { 
            no_of_reads++;
            Global_counter++;
          temp = -1;
          for(long tagIterate : cache_addresses[index]){
            temp++;
            if((tagIterate >> block_bits) == (address >> block_bits)){
                columnNum = temp;
                hitOrmiss_cache = 1;
                break;   
            }
            }
          //read hit case 
          if(hitOrmiss_cache == 1){
              updateCounters_Cache(columnNum,index,replacement_policy,1);//update counters as hit
              //System.out.println("read hit in l1");        
          }
          //miss case
          else if(hitOrmiss_cache == 0){
              //Find if Cache Empty
              
              boolean emptyblock = false;
              for(int i=0;i<no_of_tags;i++){
                  if(valid_bits[index][i] == 0) {
                      cache_array[index][i] = tag;
                      cache_addresses[index][i] = address;
                      valid_bits[index][i] = 1;
                      dirty_bits[index][i] = 0;
                      updateCounters_Cache(i,index,replacement_policy,0);//update counter as miss
                      emptyblock = true;
                      read_cache_miss += 1;
                      fwd = true;
                      //System.out.println("read miss and empty in l1");
                      break;
                  }
              
              }
              
              //L1 is not empty
              if((!emptyblock)){
                  evict_columnNum = find_evict_block(index,replacement_policy);
                  if(victimPresent == 0){
                      read_cache_miss += 1;
                      fwd = true;
                      if(dirty_bits[index][evict_columnNum] == 1){
                          
                          evict_address = cache_addresses[index][evict_columnNum];
                          writeback_bool = true;
                          write_back++;
                          
                      }
                      cache_array[index][evict_columnNum] = tag;
                      cache_addresses[index][evict_columnNum] = address;
                      valid_bits[index][evict_columnNum] = 1;
                      dirty_bits[index][evict_columnNum] = 0;
                      updateCounters_Cache(evict_columnNum,index,replacement_policy,0);//update counter as miss
                      //System.out.println("read miss and evict in l1");
                                       
                  }
                  
                      if(victimPresent == 1  ){
                      //victim hit
                      boolean victimHit = false;
                      for(int i=0;i<no_of_victim_blocks;i++){
                          if((victim_addresses[i] >> block_bits) == (address >> block_bits)){
                              victimHit = true;
                              swap++;
                              long temp_tag = victim_cache[i];
                              long temp_address = victim_addresses[i];
                              long temp_dirty = victim_dirty_bits[i];
                              long temp_counter = victim_lru_counter[i];
                              
                              victim_cache[i] = cache_array[index][evict_columnNum];
                              victim_addresses[i] = cache_addresses[index][evict_columnNum];
                              victim_valid_bits[i] = 1;
                              victim_dirty_bits[i] = dirty_bits[index][evict_columnNum];
                              victim_lru_counter[i] = 0;
                              
                              cache_array[index][evict_columnNum] = temp_tag;
                              cache_addresses[index][evict_columnNum] = temp_address;
                              valid_bits[index][evict_columnNum] = 1;
                              dirty_bits[index][evict_columnNum] = temp_dirty;
                              updateCounters_Cache(evict_columnNum,index,replacement_policy,0);
                              
                              int tempCount = -1;
                              for(long countIterate : victim_lru_counter){
                              tempCount++;
                              if((victim_lru_counter[tempCount] < temp_counter) && (tempCount != i)) victim_lru_counter[tempCount]++;
                               }
                              //System.out.println("victium hit in l1");
                              break;
                          }
                      
                      }
                      
                      if(!victimHit){
                          read_cache_miss += 1;
                          fwd = true;
                          //victim is empty 
                      boolean emptyVictim = false;
                      for(int i=0;i<no_of_victim_blocks;i++){
                          if(victim_valid_bits[i] == 0){
                              emptyVictim = true;
                              victim_cache[i] = cache_array[index][evict_columnNum];
                              victim_addresses[i] = cache_addresses[index][evict_columnNum];
                              victim_valid_bits[i] = 1;
                              victim_dirty_bits[i] = dirty_bits[index][evict_columnNum];
                              cache_array[index][evict_columnNum] = tag;
                              cache_addresses[index][evict_columnNum] = address;
                              valid_bits[index][evict_columnNum] = 1;
                              dirty_bits[index][evict_columnNum] = 0;
                              updateCounters_Cache(evict_columnNum,index,replacement_policy,0);//update counter as miss
                              int tempCount = -1;
                              for(long countIterate : victim_lru_counter){
                              tempCount++;
                              if(tempCount == i) victim_lru_counter[tempCount] = 0;
                              else victim_lru_counter[tempCount]++;
                               }
                              
                              //System.out.println("victim miss and empty  in l1");
                          break;
                          }
                      
                      }
                      
                      //victim not empty
                      if(!emptyVictim){
                          int victim_evict =0;
                          long max_count = victim_lru_counter[0];
                          for(int i=0; i<no_of_victim_blocks;i++){
                          if(max_count<victim_lru_counter[i]){victim_evict = i; max_count = victim_lru_counter[i];}
                          }
                          
                          if(victim_dirty_bits[victim_evict]==1){victim_write_back++; evict_address = victim_addresses[victim_evict]; writeback_bool = true;}
                           victim_cache[victim_evict] = cache_array[index][evict_columnNum];
                           victim_addresses[victim_evict] = cache_addresses[index][evict_columnNum];
                           victim_valid_bits[victim_evict] = 1;
                           victim_dirty_bits[victim_evict] = dirty_bits[index][evict_columnNum];
                           cache_array[index][evict_columnNum] = tag;
                           cache_addresses[index][evict_columnNum] = address;
                           valid_bits[index][evict_columnNum] = 1;
                           dirty_bits[index][evict_columnNum] = 0;
                           updateCounters_Cache(evict_columnNum,index,replacement_policy,0);//update counter as miss
                           int tempCount = -1;
                           for(long countIterate : victim_lru_counter){
                              tempCount++;
                              if(tempCount == victim_evict) victim_lru_counter[tempCount] = 0;
                              else victim_lru_counter[tempCount]++;
                               }
                           //System.out.println("victim miss and not empty in l1");
                          
                      }
                  
                  }
                  }
              
              }
          
          }
        
        
        }
        else { 
            no_of_writes++;
            Global_counter++;
                      temp = -1;
          for(long tagIterate : cache_addresses[index]){
            temp++;
            if((tagIterate >> block_bits) == (address >> block_bits)){
                columnNum = temp;
                hitOrmiss_cache = 1;
                break;   
            }
            }
          //write hit case 
          if(hitOrmiss_cache == 1){
              updateCounters_Cache(columnNum,index,replacement_policy,1);//update counters as hit
              dirty_bits[index][columnNum] = 1; 
              //System.out.println("write hit in l1");
          }
          //miss case
          else if(hitOrmiss_cache == 0){
              //Find if Cache Empty
              boolean emptyblock = false;
              for(int i=0;i<no_of_tags;i++){
                  if(valid_bits[index][i] == 0) {
                      cache_array[index][i] = tag;
                      cache_addresses[index][i] = address;
                      valid_bits[index][i] = 1;
                      dirty_bits[index][i] = 1;
                      updateCounters_Cache(i,index,replacement_policy,0);//update counter as miss
                      emptyblock = true;
                      write_cache_miss += 1;
                      fwd = true;
                      //System.out.println("write miss and empty in l1");
                      break;
                  }
              
              }
              
              //L1 is not empty
              if((!emptyblock)){
                  evict_columnNum = find_evict_block(index,replacement_policy);
                  if(victimPresent == 0){
                      if(dirty_bits[index][evict_columnNum] == 1){
                          write_back++;
                          evict_address = cache_addresses[index][evict_columnNum];
                          writeback_bool = true;
                      }
                      cache_array[index][evict_columnNum] = tag;
                      cache_addresses[index][evict_columnNum] = address;
                      valid_bits[index][evict_columnNum] = 1;
                      dirty_bits[index][evict_columnNum] = 1;
                      updateCounters_Cache(evict_columnNum,index,replacement_policy,0);//update counter as miss
                      write_cache_miss += 1; 
                      fwd = true;
                      //System.out.println("write miss and not empty in l1");
                  } 
                  
                  if(victimPresent == 1){
                      //victim hit
                      boolean victimHit = false;
                      for(int i=0;i<no_of_victim_blocks;i++){
                          if((victim_addresses[i] >> block_bits) == (address >> block_bits)){
                              victimHit = true;
                              swap++;
                              long temp_tag = victim_cache[i];
                              long temp_address = victim_addresses[i];
                              long temp_dirty = victim_dirty_bits[i];
                              long temp_counter = victim_lru_counter[i];
                              
                              victim_cache[i] = cache_array[index][evict_columnNum];
                              victim_addresses[i] = cache_addresses[index][evict_columnNum];
                              victim_valid_bits[i] = 1;
                              victim_dirty_bits[i] = dirty_bits[index][evict_columnNum];
                              victim_lru_counter[i] = 0;
                              
                              cache_array[index][evict_columnNum] = temp_tag;
                              cache_addresses[index][evict_columnNum] = temp_address;
                              valid_bits[index][evict_columnNum] = 1;
                              dirty_bits[index][evict_columnNum] = temp_dirty;
                              dirty_bits[index][evict_columnNum] = 1;
                              updateCounters_Cache(evict_columnNum,index,replacement_policy,0);
                              
                              int tempCount = -1;
                              for(long countIterate : victim_lru_counter){
                              tempCount++;
                              if(victim_lru_counter[tempCount] < temp_counter && (tempCount != i)) victim_lru_counter[tempCount]++;
                               }
                              //System.out.println("write victim hit in l1");
                              break;
                              
                              
                          }
                      
                      }
                      
                      if(!victimHit){
                          //victim is empty 
                          write_cache_miss +=1;
                          fwd = true;
                      boolean emptyVictim = false;
                      for(int i=0;i<no_of_victim_blocks;i++){
                          if(victim_valid_bits[i] == 0){
                              emptyVictim = true;
                              victim_cache[i] = cache_array[index][evict_columnNum];
                              victim_addresses[i] = cache_addresses[index][evict_columnNum];
                              victim_valid_bits[i] = 1;
                              victim_dirty_bits[i] = dirty_bits[index][evict_columnNum];
                              cache_array[index][evict_columnNum] = tag;
                              cache_addresses[index][evict_columnNum] = address;
                              valid_bits[index][evict_columnNum] = 1;
                              dirty_bits[index][evict_columnNum] = 1;
                              updateCounters_Cache(evict_columnNum,index,replacement_policy,0);//update counter as miss
                              int tempCount = -1;
                              for(long countIterate : victim_lru_counter){
                              tempCount++;
                              if(tempCount == i) victim_lru_counter[tempCount] = 0;
                              else victim_lru_counter[tempCount]++;
                               }
                               //System.out.println("write miss and victim empty in l1");
                              break;
                             
                          
                          }
                      
                      }
                      
                      //victim not empty
                      if(!emptyVictim){
                          int victim_evict =0;
                          long max_count = victim_lru_counter[0];
                          for(int i=0; i<no_of_victim_blocks;i++){
                          if(max_count<victim_lru_counter[i]){victim_evict = i; max_count = victim_lru_counter[i];}
                          }
                          
                          if(victim_dirty_bits[victim_evict]==1){victim_write_back++; evict_address = victim_addresses[victim_evict]; writeback_bool = true;}
                           victim_cache[victim_evict] = cache_array[index][evict_columnNum];
                           victim_addresses[victim_evict] = cache_addresses[index][evict_columnNum];
                           victim_valid_bits[victim_evict] = 1;
                           victim_dirty_bits[victim_evict] = dirty_bits[index][evict_columnNum];
                           cache_array[index][evict_columnNum] = tag;
                           cache_addresses[index][evict_columnNum] = address;
                           valid_bits[index][evict_columnNum] = 1;
                           dirty_bits[index][evict_columnNum] = 1;
                           updateCounters_Cache(evict_columnNum,index,replacement_policy,0);//update counter as miss
                           int tempCount = -1;
                           for(long countIterate : victim_lru_counter){
                              tempCount++;
                              if(tempCount == victim_evict) victim_lru_counter[tempCount] = 0;
                              else victim_lru_counter[tempCount]++;
                               }
                           //System.out.println("write miss and victim not empty in l1");
                          
                      }
                  
                  }
                  }
                  
                  
              
              }
          
          }
        
        }
    }
    
}
