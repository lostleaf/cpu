#include <stdio.h>
int main()
{
	int a[2][3],b[3][2],c[2][2];
	int i,j,k;
	freopen("ram_data.hex", "r", stdin);

	for (i = 0;i < 2; ++i)
		for (j = 0;j < 3; ++j)
			scanf("%d", &a[i][j]);

	for (i = 0;i < 3; i++)
		for (j = 0;j < 2; j++)
			scanf("%d", &b[i][j]);
	
	for(i=0;i<2;i++)
	{
		for(j=0;j<3;j++)
		{
			for(k=0;k<2;k++)
			{
				c[i][k] += a[i][j]*b[j][k];
			}
		}
	}

	for (i=0;i<2;i++)
		for(j=0;j<2;j++)
			printf("%d\n",c[i][j]);
	return 0;
}