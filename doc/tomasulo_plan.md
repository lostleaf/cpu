Tomasulo Plan
======

##Issue cycle

###controller

@Posedge:
 
  1. check FU 
  2. (FU, Inst) -> CDB_Inst
  
@Negedge:

  - update register status
  
###Reservation station
@Posedge

   - \#0.1 update busy, op
   - check register status to update Qk,Qj Vk,Vj 
     - if Q_j ready 
     
            put the corresponding data into Vj
            
            else  check Reservation Station of Qj
            
                if (data is calculated already i.e. not busy)
                  put the data into Vj
                else
                  Qj = wait

##Execution cycle

###@Posedge
calculate