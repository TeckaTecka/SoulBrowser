<?php
class Admin_Model_DbTable_Products2Categories extends Zend_Db_Table_Abstract
{
	protected $_name = 'products2categories';
	protected $_primary = 'id';
	/**
     * Returns primary keys
     * @param	int		$products_id
     * @param	int		$categories_id
     * @return	int
     */
	public function setProduct2Category($products_id, $categories_id)
	{
		$data = array('products_id'		=>	$products_id,
					  'categories_id'	=>	$categories_id);
    	return $this->insert($data);
	}
	/**
     * Update productBD
     * @param	int		$products_id
     * @param	int		$categories_id
     * @return	void
     */
	/*public function updateProductBD($products_id, $categories_id)
	{
		$data = array('categories_id'	=>	$categories_id);
		
		$this->update($data, 'products_id = '.$products_id);
	}
	/**
     * Returns array of product
     * @param	int	$product_id
     * @return	array
     */
	public function getProduct($product_id)
	{
		$select = $this->select();
		$select->order(array('id ASC'))
		       ->where('products_id = ?', $product_id);
		$rows = $this->fetchAll($select);
				
		if (count($rows)==0) {
			return NULL;
		} else {
			$rows = $rows->toArray();
			return $rows;
		}
	}
	/**
     * Delete productDB
     * @param	int	$id
     * @return	void
     */
	public function delProduct($product_id)
    {
    	$this->delete('products_id = '.$product_id);	
    }
	/**
     * Delete product
     * @param	int	$id
     * @return	void
     */
	public function delProductByCategoryID($category_id)
    {
    	$this->delete('categories_id = '.$category_id);	
    }
}