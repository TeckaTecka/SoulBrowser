<?php
class Admin_Model_DbTable_Orders2Products extends Zend_Db_Table_Abstract
{
	protected $_name = 'orders2products';
	protected $_primary = 'id';
	/**
     * Update count + 1
     * @param	int	$order_id
     * @param	int	$product_id
     * @return	void
     */
	public function updateCountUp($order_id, $product_id)
	{
		$select = $this->select();
		$select->where('orders_id = ?', $order_id)
			->where('products_id = ?', $product_id);
		$row = $this->fetchRow($select);

		if (count($row)==0) {
			return NULL;
		} else {
			$data = array('count'	=>	$row['count'] + 1);
			$this->update($data, 'id = '.$row['id']);
			return NULL;
		}
	}
	/**
     * Update count - 1
     * @param	int	$order_id
     * @param	int	$product_id
     * @return	void
     */
	public function updateCountDown($order_id, $product_id)
	{
		$select = $this->select();
		$select->where('orders_id = ?', $order_id)
			->where('products_id = ?', $product_id);
		$row = $this->fetchRow($select);

		if (count($row)==0) {
			return NULL;
		} else {
			$data = array('count'	=>	$row['count'] - 1);
			$this->update($data, 'id = '.$row['id']);
			return NULL;
		}
	}
	/**
     * Return price f order
     * @return	array
     */
	public function getPrice($order_id)
	{
		$select = $this->select(Zend_Db_Table::SELECT_WITH_FROM_PART)
					   ->setIntegrityCheck(false);
		$select->join('products',		  
		       		  'products.id = orders2products.products_id', array('price'))
			   ->where('orders2products.orders_id = ?', $order_id);
		$rows = $this->fetchAll($select);
		
		if (count($rows)==0) {
			return NULL;
		} else {
			$rows = $rows->toArray();
			$price = 0;
			for ($i = 0; $i < count($rows); $i++) {
				$price += $rows[$i]['price'] * $rows[$i]['count'];
			}
			return $price;
		}
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
		$select
			->join('products',		  
		       		  'products.id = orders2products.products_id', array('code','title','short_desc','price'))
			->join('vat',		  
		       		  'vat.id = products.vat_id', array('vat.title AS vat_title', 'vat.vat'))
			->where('orders2products.orders_id = ?', $order_id);
		$rows = $this->fetchAll($select);
				
		if (count($rows)==0) {
			return NULL;
		} else {
			$rows = $rows->toArray();
			for ($i=0; $i<count($rows); $i++) {
				$rows[$i]['price_vat'] = round((100 / ($rows[$i]['vat'] + 100)) * $rows[$i]['price']);
			}
			return $rows;
		}
    }
	/**
     * Delete product2orders
     * @param	int	$order_id
     * @param	int	$product_id
     * @return	void
     */
	public function delProduct($order_id, $product_id)
    {
    	$this->delete('orders_id = '.$order_id.' AND products_id = '.$product_id);
    }
}