<?php
class Admin_Model_DbTable_Products2Pictures extends Zend_Db_Table_Abstract
{
	protected $_name = 'products2pictures';
	protected $_primary = 'id';
	
	/**
     * Returns primary keys
     * @param	int	$product_id
     * @param	int	$picture_id
     * @return	int
     */
	public function setPicture($product_id, $picture_id)
	{
		$data = array(
			'products_id'	=>	$product_id,
			'pictures_id'	=>	$picture_id
		);
    	return $this->insert($data);    	
	}
	/**
     * Delete products2pictures
     * @param int	$id
     * @return	void
     */
	public function delProducts2Pictures($picture_id)
    {
    	$this->delete('pictures_id = '.$picture_id);
    }
	
}