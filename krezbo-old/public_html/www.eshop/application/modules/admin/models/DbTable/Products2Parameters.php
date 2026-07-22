<?php
class Admin_Model_DbTable_Products2Parameters extends Zend_Db_Table_Abstract
{
	protected $_name = 'products2parameters';
	protected $_primary = 'id';
	
	/**
     * Returns primary keys
     * @param	int	$product_id
     * @param	int	$parameter_id
     * @param	string	$value
     * @return	int
     */
	public function setParameter($product_id, $parameter_id, $value)
	{
		$data = array(
			'products_id'	=>	$product_id,
			'parameters_id'	=>	$parameter_id,
			'value'			=>	$value
		);
    	return $this->insert($data);    	
	}
	/**
     * Return array of product
     * @param int	$product_id
     * @return	array
     */
	public function getParameters($product_id)
	{
		$select = $this->select(Zend_Db_Table::SELECT_WITH_FROM_PART)
					   ->setIntegrityCheck(false);
		$select->join('parameters',		  
		       		  'parameters.id = products2parameters.parameters_id', array('title'))
			   ->where('products2parameters.products_id = ?', $product_id);
		$rows = $this->fetchAll($select);
		
		if (count($rows)==0) {
			return NULL;
		} else {
			$rows = $rows->toArray();
			return $rows;
		}
	}
	/**
     * Delete products2parameters
     * @param int	$id
     * @return	void
     */
	public function delParameter($id)
    {
    	$this->delete('id = '.$id);
    }
}