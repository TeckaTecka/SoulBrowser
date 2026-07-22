<?php
class Cart_Model_DbTable_Products extends Zend_Db_Table_Abstract
{
	protected $_name = 'products';
	protected $_primary = 'id';
	
	/**
     * Returns product
     * @param	int	$id
     * @return	array
     */
	public function getProduct($id)
	{
		$select = $this->select()
			->from('products', array('id', 'code', 'title', 'title_url', 'price'))
			->where('id = ?', $id);
		$row = $this->fetchRow($select);
		if ($row){
			$row = $row->toArray();
			return $row;
		}else{
			return NULL;
		}
	}
	
}