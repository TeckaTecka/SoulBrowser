<?php
class Cart_Model_DbTable_Orders2Products extends Zend_Db_Table_Abstract
{
	protected $_name = 'orders2products';
	protected $_primary = 'id';
	
	/**
     * Add row to DB
     * @param	int		$orders_id
     * @param	int		$products_id
     * @param	int		$count
     * @return	primary key
     */
	public function setOrders2Products($orders_id, $products_id, $count)
    {
    	$data = array(
    		'orders_id'		=>	$orders_id,
    		'products_id'	=>	$products_id,
    		'count'			=>	$count
    	);
        return $this->insert($data);
    }
	/**
     * get Products
     * @param	int		$order_id
     * @return	array
     */
	public function getProductsByOrderID($order_id)
    {
    	$select = $this->select(Zend_Db_Table::SELECT_WITH_FROM_PART)
					   ->setIntegrityCheck(false);
		$select->join('products',		  
		       		  'products.id = orders2products.products_id', array('code','title','short_desc','price'))
			   ->where('orders2products.orders_id = ?', $order_id);
		$rows = $this->fetchAll($select);
				
		if (count($rows)==0) {
			return NULL;
		} else {
			$rows = $rows->toArray();
			return $rows;
		}
    }
}