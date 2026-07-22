<?php
class Admin_Model_DbTable_Orders extends Zend_Db_Table_Abstract
{
	protected $_name = 'orders';
	protected $_primary = 'id';
	/**
     * Return array of orders
     * @return	array
     */
	public function getOrders($page)
	{
		$select = $this->select(Zend_Db_Table::SELECT_WITH_FROM_PART)
					   ->setIntegrityCheck(false);
		$select->join('payment',		  
		       		  'payment.id = orders.payment_id', array('payment', 'price AS pay_price'))
			   ->join('consumption',		  
		       		  'consumption.id = orders.consumption_id', array('consumption', 'price AS cons_price'))
			   ->join('orders_statuses',		  
		       		  'orders_statuses.id = orders.orders_statuses_id', array('status'))
			   ->limitPage($page, 20)
			   ->order('orders.id DESC');
		$rows = $this->fetchAll($select);
		
		if (count($rows)==0) {
			return NULL;
		} else {
			$rows = $rows->toArray();
			$orders2ProductsTab = new Admin_Model_DbTable_Orders2Products();
			for ($i=0;$i<count($rows);$i++) {
				$rows[$i]['price'] = $orders2ProductsTab->getPrice($rows[$i]['id']);
				$rows[$i]['price'] += (($rows[$i]['pay_price'])?$rows[$i]['pay_price']:0);
				$rows[$i]['price'] += (($rows[$i]['cons_price'])?$rows[$i]['cons_price']:0);
				$rows[$i]['price'] = number_format($rows[$i]['price'], 0, '.', ' ').' '.$this->getCurrency();
			}
			return $rows;
		}
	}
	/**
     * Return array of new orders
     * @return	array
     */
	public function getNewOrders()
	{
		$select = $this->select(Zend_Db_Table::SELECT_WITH_FROM_PART)
					   ->setIntegrityCheck(false);
		$select->join('payment',		  
		       		  'payment.id = orders.payment_id', array('payment', 'price AS pay_price'))
			   ->join('consumption',		  
		       		  'consumption.id = orders.consumption_id', array('consumption', 'price AS cons_price'))
			   ->join('orders_statuses',		  
		       		  'orders_statuses.id = orders.orders_statuses_id', array('status'))
			   ->where('orders_statuses.id = 1 OR orders_statuses.id = 2')
			   ->order('orders.id DESC');
		$rows = $this->fetchAll($select);
		
		if (count($rows)==0) {
			return NULL;
		} else {
			$rows = $rows->toArray();
			$orders2ProductsTab = new Admin_Model_DbTable_Orders2Products();
			for ($i=0;$i<count($rows);$i++) {
				$rows[$i]['price'] = $orders2ProductsTab->getPrice($rows[$i]['id']);
				$rows[$i]['price'] += (($rows[$i]['pay_price'])?$rows[$i]['pay_price']:0);
				$rows[$i]['price'] += (($rows[$i]['cons_price'])?$rows[$i]['cons_price']:0);
				$rows[$i]['price'] = number_format($rows[$i]['price'], 0, '.', ' ').' '.$this->getCurrency();
			}
			return $rows;
		}
	}
	/**
     * Return array of order
     * @return	array
     */
	public function getOrder($order_id)
	{
		$select = $this->select(Zend_Db_Table::SELECT_WITH_FROM_PART)
					   ->setIntegrityCheck(false);
		$select->join('payment',		  
		       		  'payment.id = orders.payment_id', array('payment', 'price AS pay_price'))
			   ->join('consumption',		  
		       		  'consumption.id = orders.consumption_id', array('consumption', 'price AS cons_price'))
			   ->join('orders_statuses',		  
		       		  'orders_statuses.id = orders.orders_statuses_id', array('status'))
			   ->where('orders.id = ?', $order_id);
		$row = $this->fetchRow($select);
		
		if (count($row)==0) {
			return NULL;
		} else {
			$row = $row->toArray();
			return $row;
		}
	}
	/**
     * Update order
     * @param	int		$id
     * @param	int		$consumption_id
     * @param	int		$payment_id
     * @param	int		$status_id
     * @return	void
     */
	public function updateOrder($id, $consumption_id, $payment_id, $status_id)
	{
		$data = array(
			'consumption_id'		=>	$consumption_id,
			'payment_id'			=>	$payment_id,
			'orders_statuses_id'	=>	$status_id
		);
		
		$this->update($data, 'id = '.$id);
	}
	/**
     * Return currency
     * @return	string
     */
    private function getCurrency()
    {
    	$settingsTab = new Admin_Model_DbTable_Settings();
        $currency = $settingsTab->getFlag('eshop');
        return $currency['currency'];
    }
}