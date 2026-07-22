<?php
class Cart_Model_DbTable_Orders extends Zend_Db_Table_Abstract
{
	protected $_name = 'orders';
	protected $_primary = 'id';
	
	/**
     * Add order to DB
     * @param	int	$consumption_id
     * @param	int	$payment_id
     * @param	int	$orders_status = 1
     * @return	primary key
     */
	public function setOrder($consumption_id, $payment_id, $orders_status = 1)
    {
    	$data = array(
    		'consumption_id'		=>	$consumption_id,
    		'payment_id'			=>	$payment_id,
    		'orders_statuses_id'	=>	$orders_status
    	);
        return $this->insert($data);
    }
	/**
     * get order
     * @param	int		$id
     * @return	primary key
     */
	public function getOrder($id)
    {
    	$select = $this->select();
		$select->where('id = ?', $id);
		$row = $this->fetchRow($select);
				
		if (count($row)==0) {
			return NULL;
		} else {
			$row = $row->toArray();
			return $row;
		}
    }
}